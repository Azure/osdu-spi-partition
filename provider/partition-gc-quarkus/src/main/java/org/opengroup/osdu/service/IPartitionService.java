package org.opengroup.osdu.service;

import java.util.List;
import org.opengroup.osdu.model.PartitionInfo;

public interface IPartitionService {
  List<String> getPartitionList();

  PartitionInfo getPartition(String partitionId);
}
