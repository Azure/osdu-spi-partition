/*
 *  Copyright 2020-2025 Google LLC
 *  Copyright 2020-2025 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.model.PartitionInfo;
import org.opengroup.osdu.model.exception.AppException;

@QuarkusTest
class PartitionServiceImplTest {
  private static final String TEST_PARTITION_1 = "osdu";
  private static final String TEST_PARTITION_2 = "second";
  private static final List<String> PARTITIONS_PATHS = List.of("/test/path,/test/data");
  private static final String NON_EXISTENT_PARTITION = "nonexistent";
  private static final int NOT_FOUND_CODE = 404;
  private static final String NOT_FOUND_MESSAGE = "Partition does not exist";

  @InjectMock DirectoryWatchService mockDirectoryWatchService;
  @InjectMock PartitionFileLoaderService mockPartitionFileLoaderService;
  @InjectMock PartitionConfigProvider mockPartitionConfigProvider;

  @Inject PartitionServiceImpl partitionService;

  private Map<String, PartitionInfo> testPartitions;
  private PartitionInfo mockPartitionInfo1;
  private PartitionInfo mockPartitionInfo2;

  @BeforeEach
  void setUp() {
    mockPartitionInfo1 = mock(PartitionInfo.class);
    mockPartitionInfo2 = mock(PartitionInfo.class);
    testPartitions =
        Map.of(TEST_PARTITION_1, mockPartitionInfo1, TEST_PARTITION_2, mockPartitionInfo2);

    when(mockPartitionConfigProvider.getPartitionConfigsPaths()).thenReturn(PARTITIONS_PATHS);
  }

  @Test
  void should_updatePartitionInfoMap_when_initCalled() {
    when(mockPartitionFileLoaderService.loadPartitionInfoMapFromFiles(PARTITIONS_PATHS))
        .thenReturn(testPartitions);

    partitionService.init();

    List<String> result = partitionService.getPartitionList();
    assertEquals(List.of(TEST_PARTITION_1, TEST_PARTITION_2), result);

    verify(mockPartitionFileLoaderService, atLeastOnce())
        .loadPartitionInfoMapFromFiles(PARTITIONS_PATHS);
    verify(mockDirectoryWatchService, atLeastOnce()).watchDirectories(eq(PARTITIONS_PATHS), any());
  }

  @Test
  void should_returnPartitionList_when_partitionsPresent() {
    partitionService.updatePartitionInfoMap(testPartitions);

    List<String> result = partitionService.getPartitionList();

    assertTrue(result.contains(TEST_PARTITION_1));
    assertTrue(result.contains(TEST_PARTITION_2));
    assertEquals(2, result.size());
  }

  @Test
  void should_returnEmptyList_when_noPartitionsPresent() {
    partitionService.updatePartitionInfoMap(Collections.emptyMap());

    List<String> result = partitionService.getPartitionList();

    assertTrue(result.isEmpty());
  }

  @Test
  void should_returnPartition_when_partitionExists() {
    partitionService.updatePartitionInfoMap(testPartitions);

    PartitionInfo result1 = partitionService.getPartition(TEST_PARTITION_1);
    PartitionInfo result2 = partitionService.getPartition(TEST_PARTITION_2);

    assertEquals(mockPartitionInfo1, result1);
    assertEquals(mockPartitionInfo2, result2);
  }

  @Test
  void should_throwAppException_when_partitionIdNotFound() {
    partitionService.updatePartitionInfoMap(testPartitions);
    AppException thrown =
        assertThrows(
            AppException.class, () -> partitionService.getPartition(NON_EXISTENT_PARTITION));
    assertEquals(NOT_FOUND_CODE, thrown.getError().getCode());
    assertTrue(thrown.getMessage().contains(NOT_FOUND_MESSAGE));
  }
}
