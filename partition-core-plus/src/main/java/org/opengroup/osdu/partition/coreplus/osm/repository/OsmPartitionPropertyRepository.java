/*
 * Copyright 2020-2021 Google LLC
 * Copyright 2020-2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.coreplus.osm.repository;


import static org.opengroup.osdu.core.osm.core.model.where.predicate.Eq.eq;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.core.model.query.GetQuery;
import org.opengroup.osdu.core.osm.core.model.where.Where;
import org.opengroup.osdu.core.osm.core.service.Context;
import org.opengroup.osdu.core.osm.core.service.Transaction;
import org.opengroup.osdu.core.osm.core.translate.TranslatorException;
import org.opengroup.osdu.partition.coreplus.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.coreplus.osm.config.provider.OsmPartitionDestinationProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsmPartitionPropertyRepository {

    public static final String PARTITION_ID_FILED = "partition_id";
    public static final String PROPERTY_ID = "id";
    private final OsmPartitionDestinationProvider osmPartitionDestinationProvider;
    private final Context context;


    public Optional<List<PartitionPropertyEntity>> findByPartitionId(String partitionId) {
        List<PartitionPropertyEntity> properties =
                context.getResultsAsList(buildPartitionEntityQueryBy(eq(PARTITION_ID_FILED, partitionId)));
        return (CollectionUtils.isEmpty(properties)) ?
                Optional.empty():
                Optional.of(properties);
    }

    public PartitionPropertyEntity findByPartitionIdAndName(String partitionId, String key) {
        return context.getOne(buildPartitionEntityQueryBy(eq(PROPERTY_ID, partitionId + "-" + key)));
    }

    public List<String> getAllPartitions() {
        return context.getResultsAsList(
                    new GetQuery<>(PartitionPropertyEntity.class, osmPartitionDestinationProvider.getDestination()))
                .stream()
                .map(PartitionPropertyEntity::getPartitionId)
                .distinct()
                .collect(Collectors.toList());
    }

    public void deleteByPartitionId(String partitionId) {
        Destination destination = osmPartitionDestinationProvider.getDestination();
        Transaction tx = null;
        try{

            tx = context.beginTransaction(destination);

            context.delete(
                    PartitionPropertyEntity.class,
                    osmPartitionDestinationProvider.getDestination(),
                    eq(PARTITION_ID_FILED, partitionId));

            tx.commitIfActive();
        } catch (TranslatorException e) {
            log.error("Error during partition delete", e);
            throw new AppException(
                    INTERNAL_SERVER_ERROR.value(),
                    INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    "Error during partition delete");
        } finally {
            if (ObjectUtils.isNotEmpty(tx)){
                tx.rollbackIfActive();
            }
        }
    }

    public void saveAll(List<PartitionPropertyEntity> partitionProperties) {
        Destination destination = osmPartitionDestinationProvider.getDestination();
        Transaction tx = null;
        try{
            tx = context.beginTransaction(destination);
            for (PartitionPropertyEntity entity : partitionProperties){
                context.upsert(entity, destination);
            }
            tx.commitIfActive();
        }
        finally {
            if (ObjectUtils.isNotEmpty(tx)){
                tx.rollbackIfActive();
            }
        }
    }

    private GetQuery<PartitionPropertyEntity> buildPartitionEntityQueryBy(Where where){
        return new GetQuery<>(PartitionPropertyEntity.class, osmPartitionDestinationProvider.getDestination(), where);
    }
}
