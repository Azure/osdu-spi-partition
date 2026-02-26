/*
  Copyright © Microsoft Corporation
  Copyright 2002-2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.opengroup.osdu.partition.coreplus.osm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.osm.core.translate.TranslatorException;
import org.opengroup.osdu.core.osm.core.model.query.GetQuery;
import org.opengroup.osdu.core.osm.core.model.where.Where;
import org.opengroup.osdu.partition.coreplus.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.coreplus.osm.config.provider.OsmPartitionDestinationProvider;
import org.opengroup.osdu.core.osm.core.service.Context;
import org.opengroup.osdu.core.osm.core.service.Transaction;
import org.opengroup.osdu.core.osm.core.model.Destination;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.same;

@ExtendWith(MockitoExtension.class)
public class OsmPartitionPropertyRepositoryTest {

    @Mock
    private OsmPartitionDestinationProvider provider;
    @Mock private Context context;
    @Mock private Destination destination;
    @Mock private Transaction tx;

    @InjectMocks
    private OsmPartitionPropertyRepository repo;

    @BeforeEach
    void setUp() {
        when(provider.getDestination()).thenReturn(destination);
    }

    @Test
    void findByPartitionId_returnsEmptyWhenNoResults() {
        when(context.getResultsAsList(any(GetQuery.class))).thenReturn(emptyList());

        Optional<List<PartitionPropertyEntity>> result = repo.findByPartitionId("p-1");

        assertTrue(result.isEmpty());
        verify(context).getResultsAsList(any(GetQuery.class));
    }

    @Test
    void findByPartitionId_returnsListWhenFound() {
        PartitionPropertyEntity e1 = new PartitionPropertyEntity();
        PartitionPropertyEntity e2 = new PartitionPropertyEntity();
        when(context.getResultsAsList(any(GetQuery.class))).thenReturn(asList(e1, e2));

        Optional<List<PartitionPropertyEntity>> result = repo.findByPartitionId("p-1");

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(context).getResultsAsList(any(GetQuery.class));
    }

    @Test
    void findByPartitionIdAndName_queriesByCompositeId() {
        PartitionPropertyEntity entity = new PartitionPropertyEntity();
        when(context.getOne(any(GetQuery.class))).thenReturn(entity);

        PartitionPropertyEntity out = repo.findByPartitionIdAndName("p-1", "k");

        assertSame(entity, out);
    }

    @Test
    void getAllPartitions_returnsDistinctIds() {
        PartitionPropertyEntity e1 = new PartitionPropertyEntity(); e1.setPartitionId("A");
        PartitionPropertyEntity e2 = new PartitionPropertyEntity(); e2.setPartitionId("B");
        PartitionPropertyEntity e3 = new PartitionPropertyEntity(); e3.setPartitionId("A"); // duplicate

        when(context.getResultsAsList(any(GetQuery.class))).thenReturn(asList(e1, e2, e3));

        List<String> ids = repo.getAllPartitions();

        assertEquals(2, ids.size());
        assertTrue(ids.contains("A"));
        assertTrue(ids.contains("B"));
        verify(context).getResultsAsList(any(GetQuery.class));
        verify(provider).getDestination();
    }

    @Test
    void deleteByPartitionId_success_commitsAndRollsBackIfNeeded() throws TranslatorException {
        when(context.beginTransaction(destination)).thenReturn(tx);

        repo.deleteByPartitionId("p-1");

        verify(context).beginTransaction(destination);
        verify(context).delete(eq(PartitionPropertyEntity.class), eq(destination), any(Where.class));
        verify(tx).commitIfActive();
        // finally-block should still call rollbackIfActive safely
        verify(tx).rollbackIfActive();
    }

    @Test
    void saveAll_upsertsEachEntity_andCommits_thenRollbacksSafely() throws TranslatorException {
        when(provider.getDestination()).thenReturn(destination);
        when(context.beginTransaction(destination)).thenReturn(tx);

        PartitionPropertyEntity e1 = new PartitionPropertyEntity();
        PartitionPropertyEntity e2 = new PartitionPropertyEntity();

        repo.saveAll(java.util.Arrays.asList(e1, e2));

        verify(context).beginTransaction(destination);
        verify(context, times(1)).upsert(same(e1), eq(destination));
        verify(context, times(1)).upsert(same(e2), eq(destination));
        verify(tx).commitIfActive();
        // finally-block always calls rollbackIfActive safely
        verify(tx).rollbackIfActive();
        verifyNoMoreInteractions(context, tx);
    }
}
