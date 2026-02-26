/*
  Copyright 2002-2021 Google LLC
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

package org.opengroup.osdu.partition.logging;

import static java.lang.String.format;

import com.google.common.base.Strings;
import java.util.List;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload.AuditPayloadBuilder;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;


public class AuditEvents {

  private static final String UNKNOWN = "unknown";
  private static final String UNKNOWN_IP = "0.0.0.0";

  private static final String CREATE_PARTITION_ACTION_ID = "PT001";
  private static final String CREATE_PARTITION_MESSAGE = "Create partition";

  private static final String READ_PARTITION_ACTION_ID = "PT002";
  private static final String READ_PARTITION_MESSAGE = "Read partition";

  private static final String DELETE_PARTITION_ACTION_ID = "PT003";
  private static final String DELETE_PARTITION_MESSAGE = "Delete partition";

  private static final String READ_SERVICE_LIVENESS_ACTION_ID = "PT004";
  private static final String READ_SERVICE_LIVENESS_MESSAGE = "Service run";

  private static final String UPDATE_PARTITION_ACTION_ID = "PT005";
  private static final String UPDATE_PARTITION_MESSAGE = "Update partition";

  private static final String READ_LIST_PARTITION_ACTION_ID = "PT006";
  private static final String READ_LIST_PARTITION_MESSAGE = "Read partition list";

  private final String user;
  private final String userIpAddress;
  private final String userAgent;
  private final String userAuthorizedGroupName;

  public AuditEvents(String user, String userIpAddress, String userAgent, String userAuthorizedGroupName) {
    this.user = Strings.isNullOrEmpty(user) ? UNKNOWN : user;
    this.userIpAddress = Strings.isNullOrEmpty(userIpAddress) ? UNKNOWN_IP : userIpAddress;
    this.userAgent = Strings.isNullOrEmpty(userAgent) ? UNKNOWN : userAgent;
    this.userAuthorizedGroupName = Strings.isNullOrEmpty(userAuthorizedGroupName) ? UNKNOWN : userAuthorizedGroupName;
  }

  /**
   * Creates an AuditPayload builder pre-populated with common audit fields.
   */
  private AuditPayloadBuilder createAuditPayloadBuilder(
      List<String> requiredGroupsForAction, AuditStatus status, String actionId) {
    return AuditPayload.builder()
        .status(status)
        .user(this.user)
        .actionId(actionId)
        .requiredGroupsForAction(requiredGroupsForAction)
        .userIpAddress(this.userIpAddress)
        .userAgent(this.userAgent)
        .userAuthorizedGroupName(this.userAuthorizedGroupName);
  }

  public AuditPayload getCreatePartitionEvent(AuditStatus status, List<String> resources) {
    return createAuditPayloadBuilder(AuditOperation.CREATE_PARTITION.getRequiredGroups(), status, CREATE_PARTITION_ACTION_ID)
        .action(AuditAction.CREATE)
        .message(getStatusMessage(status, CREATE_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getReadPartitionEvent(AuditStatus status, List<String> resources) {
    return createAuditPayloadBuilder(AuditOperation.READ_PARTITION.getRequiredGroups(), status, READ_PARTITION_ACTION_ID)
        .action(AuditAction.READ)
        .message(getStatusMessage(status, READ_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getDeletePartitionEvent(AuditStatus status, List<String> resources) {
    return createAuditPayloadBuilder(AuditOperation.DELETE_PARTITION.getRequiredGroups(), status, DELETE_PARTITION_ACTION_ID)
        .action(AuditAction.DELETE)
        .message(getStatusMessage(status, DELETE_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getReadServiceLivenessEvent(AuditStatus status, List<String> resources) {
    return createAuditPayloadBuilder(AuditOperation.READ_SERVICE_LIVENESS.getRequiredGroups(), status, READ_SERVICE_LIVENESS_ACTION_ID)
        .action(AuditAction.READ)
        .message(getStatusMessage(status, READ_SERVICE_LIVENESS_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getUpdatePartitionSecretEvent(AuditStatus status, List<String> resources) {
    return createAuditPayloadBuilder(AuditOperation.UPDATE_PARTITION.getRequiredGroups(), status, UPDATE_PARTITION_ACTION_ID)
        .action(AuditAction.UPDATE)
        .message(getStatusMessage(status, UPDATE_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getListPartitionEvent(AuditStatus status, List<String> resources) {
    return createAuditPayloadBuilder(AuditOperation.READ_LIST_PARTITION.getRequiredGroups(), status, READ_LIST_PARTITION_ACTION_ID)
        .action(AuditAction.READ)
        .message(getStatusMessage(status, READ_LIST_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  private String getStatusMessage(AuditStatus status, String message) {
    return format("%s - %s", message, status.name().toLowerCase());
  }
}
