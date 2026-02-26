/*
 * Copyright © Amazon Web Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.opengroup.osdu.partition.provider.aws.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.aws.v2.dynamodb.DynamoDBConfig;
import org.opengroup.osdu.core.aws.v2.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.aws.v2.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.v2.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.partition.provider.aws.model.PartitionDoc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartitionServiceImplTest {

    private static final String PARTITION_ID = "test-partition";
    private static final String REGION = "us-east-1";
    private static final String TABLE_NAME = "test-table";

    @Mock
    private JaxRsDpsLog logger;

    @Mock
    private DynamoDBQueryHelper<PartitionDoc> queryHelper;

    @Captor
    private ArgumentCaptor<PartitionDoc> partitionDocCaptor;

    private PartitionServiceImpl partitionService;
    private PartitionInfo partitionInfo;

    @BeforeEach
    void setUp() throws K8sParameterNotFoundException {
        try (MockedConstruction<K8sLocalParameterProvider> paramMock = Mockito.mockConstruction(
                K8sLocalParameterProvider.class,
                (provider, context) -> {
                    when(provider.getParameterAsString("DYNAMODB_TABLE_NAME")).thenReturn(TABLE_NAME);
                });
             MockedConstruction<DynamoDBQueryHelper> helperMock = Mockito.mockConstruction(
                DynamoDBQueryHelper.class,
                (helper, context) -> {
                    // Redirect all calls to our mock
                    when(helper.getItem(anyString())).thenAnswer(inv -> queryHelper.getItem(inv.getArgument(0)));
                    doAnswer(inv -> {
                        queryHelper.putItem((PartitionDoc) inv.getArgument(0));
                        return null;
                    }).when(helper).putItem(any(PartitionDoc.class));
                    doAnswer(inv -> {
                        queryHelper.deleteItem((String) inv.getArgument(0));
                        return null;
                    }).when(helper).deleteItem(anyString());
                    when(helper.scanTable()).thenAnswer(inv -> queryHelper.scanTable());
                })) {

            ProviderConfigurationBag config = new ProviderConfigurationBag(REGION);
            partitionService = new PartitionServiceImpl(logger, config);
        }

        // Setup test partition info
        Map<String, Property> properties = new HashMap<>();
        properties.put("storageAccount", Property.builder()
                .value("test-storage")
                .sensitive(false)
                .build());
        partitionInfo = PartitionInfo.builder()
                .properties(properties)
                .build();
    }

    @Test
    void createPartition_Success() {
        // Arrange
        when(queryHelper.getItem(PARTITION_ID)).thenReturn(Optional.empty());

        // Act
        PartitionInfo result = partitionService.createPartition(PARTITION_ID, partitionInfo);

        // Assert
        assertNotNull(result);
        verify(queryHelper).putItem(partitionDocCaptor.capture());
        assertEquals(PARTITION_ID, partitionDocCaptor.getValue().getId());
        assertEquals(partitionInfo, partitionDocCaptor.getValue().getPartitionInfo());
    }

    @Test
    void createPartition_ThrowsException_WhenPartitionExists() {
        // Arrange
        PartitionDoc existingDoc = PartitionDoc.create(PARTITION_ID, partitionInfo);
        when(queryHelper.getItem(PARTITION_ID)).thenReturn(Optional.of(existingDoc));

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.createPartition(PARTITION_ID, partitionInfo));
        assertEquals(409, exception.getError().getCode());
        assertEquals("partition exist", exception.getError().getReason());
    }

    @Test
    void updatePartition_Success_WithNewProperties() {
        // Arrange
        PartitionDoc existingDoc = PartitionDoc.create(PARTITION_ID, partitionInfo);
        when(queryHelper.getItem(PARTITION_ID)).thenReturn(Optional.of(existingDoc));

        Map<String, Property> newProperties = new HashMap<>();
        newProperties.put("newKey", Property.builder().value("newValue").sensitive(false).build());
        PartitionInfo updateInfo = PartitionInfo.builder().properties(newProperties).build();

        // Act
        PartitionInfo result = partitionService.updatePartition(PARTITION_ID, updateInfo);

        // Assert
        assertNotNull(result);
        assertTrue(result.getProperties().containsKey("newKey"));
        verify(queryHelper).putItem(any(PartitionDoc.class));
    }

    @Test
    void updatePartition_SkipsWrite_WhenNoNewProperties() {
        // Arrange
        PartitionDoc existingDoc = PartitionDoc.create(PARTITION_ID, partitionInfo);
        when(queryHelper.getItem(PARTITION_ID)).thenReturn(Optional.of(existingDoc));

        // Act - try to update with same properties
        PartitionInfo result = partitionService.updatePartition(PARTITION_ID, partitionInfo);

        // Assert
        assertNotNull(result);
        verify(queryHelper, never()).putItem(any(PartitionDoc.class));
    }

    @Test
    void updatePartition_ThrowsException_WhenPartitionNotFound() {
        // Arrange
        when(queryHelper.getItem(PARTITION_ID)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.updatePartition(PARTITION_ID, partitionInfo));
        assertEquals(404, exception.getError().getCode());
    }

    @Test
    void updatePartition_ThrowsException_WhenIdUpdateAttempted() {
        // Arrange
        Map<String, Property> properties = new HashMap<>();
        properties.put("id", Property.builder().value("new-id").build());
        PartitionInfo invalidInfo = PartitionInfo.builder().properties(properties).build();

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.updatePartition(PARTITION_ID, invalidInfo));
        assertEquals(400, exception.getError().getCode());
    }

    @Test
    void getPartition_Success() {
        // Arrange
        PartitionDoc existingDoc = PartitionDoc.create(PARTITION_ID, partitionInfo);
        when(queryHelper.getItem(PARTITION_ID)).thenReturn(Optional.of(existingDoc));

        // Act
        PartitionInfo result = partitionService.getPartition(PARTITION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(partitionInfo, result);
    }

    @Test
    void getPartition_ThrowsException_WhenNotFound() {
        // Arrange
        when(queryHelper.getItem(PARTITION_ID)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.getPartition(PARTITION_ID));
        assertEquals(404, exception.getError().getCode());
    }

    @Test
    void deletePartition_Success() {
        // Arrange
        PartitionDoc existingDoc = PartitionDoc.create(PARTITION_ID, partitionInfo);
        when(queryHelper.getItem(PARTITION_ID)).thenReturn(Optional.of(existingDoc));

        // Act
        boolean result = partitionService.deletePartition(PARTITION_ID);

        // Assert
        assertTrue(result);
        verify(queryHelper).deleteItem(PARTITION_ID);
    }

    @Test
    void deletePartition_ThrowsException_WhenNotFound() {
        // Arrange
        when(queryHelper.getItem(PARTITION_ID)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.deletePartition(PARTITION_ID));
        assertEquals(404, exception.getError().getCode());
    }

    @Test
    void getAllPartitions_Success() {
        // Arrange
        ArrayList<PartitionDoc> partitions = new ArrayList<>();
        partitions.add(PartitionDoc.create("partition1", partitionInfo));
        partitions.add(PartitionDoc.create("partition2", partitionInfo));

        when(queryHelper.scanTable()).thenReturn(partitions);

        // Act
        List<String> result = partitionService.getAllPartitions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("partition1"));
        assertTrue(result.contains("partition2"));
    }

    @Test
    void getAllPartitions_ThrowsException_WhenQueryHelperFails() {
        // Arrange
        String errorMessage = "DynamoDB scan operation failed";
        when(queryHelper.scanTable())
                .thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.getAllPartitions());

        // Verify exception details
        assertEquals(500, exception.getError().getCode());
        assertEquals("Failed to retrieve partitions", exception.getError().getReason());
        assertEquals(errorMessage, exception.getError().getMessage());

        // Verify logging
        verify(logger).error(eq("Failed to retrieve all partitions"), any(RuntimeException.class));
    }
}
