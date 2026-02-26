// Copyright 2017-2020, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, eitsher express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.provider.azure.persistence;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableTransactionAction;
import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;

import jakarta.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataTableStoreTest {

    @Mock
    private TableClient tableClient;

    @InjectMocks
    private DataTableStore sut;

    @Test
    public void should_empty_whenRecordNotExists() {
        PagedIterable<TableEntity> response = mock(PagedIterable.class);
        when(tableClient.listEntities(any(), any(), any())).thenReturn(response);

        Iterable<TableEntity> results = sut.queryByKey("partitionKey", "partitionId");

        assertNotNull(results);
    }

    @Test
    public void shouldNotThrowException_whenEmptyRecordsFor_insertBatchEntities() {
        List<TableTransactionAction> actionList = new ArrayList<>();
        assertDoesNotThrow(() -> sut.insertBatchEntities(actionList));
    }

    @Test
    public void shouldThrowAppException500_whenExceptionOccurredFor_insertBatchEntities() {
        when(tableClient.submitTransaction(any())).thenThrow(AppException.class);
        List<TableTransactionAction> actionList = new ArrayList<>();

        AppException appException = assertThrows(AppException.class, () -> sut.insertBatchEntities(actionList));

        assertNotNull(appException);
        assertEquals(500, appException.getError().getCode());
        assertEquals("error creating partition", appException.getError().getReason());
    }

    @Test
    public void when_call_queryByCompoundKey() {
        PagedIterable<TableEntity> response = mock(PagedIterable.class);
        when(tableClient.listEntities(any(), any(), any())).thenReturn(response);

        Iterable<TableEntity> result = sut.queryByCompoundKey("RowKey", "id", "value", "partitionId");

        assertNotNull(result);
    }

    @Test
    public void when_wrongInput_queryByCompoundKey_ReturnNull() {
        try {
            Iterable<TableEntity> output = sut.queryByCompoundKey(null, null, null, null);
            Assert.isNull(output);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }


    @Test
    public void when_call_queryByKey_wrongInput_ShouldReturnNull() {
        try {
            Iterable<TableEntity> output = sut.queryByKey(null, null);
            assertNull(output);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void when_queryByKey_invalid_input_ExceptionIsThrown() {
        try {
            sut.queryByKey("partitionKey", "invalid '");
        } catch (Exception e) {
            assertNotNull(e);
            assertEquals(e.getClass(), ValidationException.class);
            assertEquals("Invalid input parameters, value contains illegal character(s)", e.getMessage());

        }
    }
    @Test
    public void shouldThrowAppException500_whenExceptionOccurredFor_queryByKey() {
        doThrow(AppException.class).when(tableClient).listEntities(any(), any(), any());

        AppException appException = assertThrows(AppException.class, () -> sut.queryByKey("partitionKey", "value1"));

        assertNotNull(appException);
        assertEquals(500, appException.getError().getCode());
        assertEquals("error listing entities by filter: partitionKey eq 'value1'", appException.getError().getReason());
    }

    @Test
    public void when_queryByCompoundKey_invalid_input_ExceptionIsThrown() {
        try {
            sut.queryByCompoundKey("rowKey", "id", "partitionKey", "invalid '");
        } catch (Exception e) {
            assertNotNull(e);
            assertEquals(e.getClass(), ValidationException.class);
            assertEquals("Invalid input parameters, value contains illegal character(s)", e.getMessage());
        }
    }

    @Test
    public void when_queryByCompoundKey_invalid_rowValyeinput_ExceptionIsThrown() {
        try {
            sut.queryByCompoundKey("rowKey", "invalid '", "partitionKey", "value1");
        } catch (Exception e) {
            assertNotNull(e);
            assertEquals(e.getClass(), ValidationException.class);
            assertEquals("Invalid input parameters, value contains illegal character(s)", e.getMessage());
        }
    }

    @Test
    public void shouldThrowAppException500_whenExceptionOccurredFor_queryByCompoundKey() {

        doThrow(AppException.class).when(tableClient).listEntities(any(), any(), any());

        AppException appException = assertThrows(AppException.class,
                () -> sut.queryByCompoundKey("rowKey", "id", "partitionKey", "value1"));

        assertNotNull(appException);
        assertEquals(500, appException.getError().getCode());
        assertEquals("error getting partition", appException.getError().getReason());
    }

    @Test
    public void shouldReturnTrue_whenDeleteEntityIsSuccess() {
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";

        boolean isDeleted = sut.deleteCloudTableEntity(partitionKey, rowKey);

        assertTrue(isDeleted);
        verify(tableClient, times(1)).deleteEntity(any());
    }

    @Test
    public void shouldThrowAppException500_whenExceptionOccurredDeleteEntity() {
        String partitionKey = "partitionKey";
        String rowKey = "rowKey";
        doThrow(AppException.class).when(tableClient).deleteEntity(any());

        AppException appException = assertThrows(AppException.class, () -> sut.deleteCloudTableEntity(partitionKey, rowKey));

        assertEquals(500, appException.getError().getCode());
        assertEquals("Error querying cloud table", appException.getError().getReason());
        verify(tableClient, times(1)).deleteEntity(any());
    }
}
