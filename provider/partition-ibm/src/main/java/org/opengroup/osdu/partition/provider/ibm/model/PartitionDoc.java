/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.provider.ibm.model;

import org.opengroup.osdu.partition.model.PartitionInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartitionDoc {
	private String _id;
	private String _rev;
	PartitionInfo partitionInfo;
	
	public PartitionDoc(String partitionId, PartitionInfo partitionInfo) {
		this._id = partitionId;
		this.partitionInfo = partitionInfo;
	}
	

}
