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

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.util.IpAddressUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class AuditLogger {

  private final JaxRsDpsLog logger;
  private final DpsHeaders headers;
  private final HttpServletRequest httpServletRequest;

  private AuditEvents events = null;

  private AuditEvents getAuditEvents() {
    if (this.events == null) {
      String userIpAddress = IpAddressUtil.getClientIpAddress(httpServletRequest);
      String userAgent = httpServletRequest.getHeader("user-agent");
      this.events = new AuditEvents("partitionAccountUser",
                                    userIpAddress, userAgent,
                                    headers.getUserAuthorizedGroupName());
    }
    return this.events;
  }

  public void createPartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getCreatePartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void createPartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getCreatePartitionEvent(AuditStatus.FAILURE, resources));
  }

  public void readPartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadPartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void readPartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadPartitionEvent(AuditStatus.FAILURE, resources));
  }

  public void deletePartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getDeletePartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void deletePartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getDeletePartitionEvent(AuditStatus.FAILURE, resources));
  }

  public void readServiceLivenessSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadServiceLivenessEvent(AuditStatus.SUCCESS, resources));
  }

  public void readServiceLivenessFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadServiceLivenessEvent(AuditStatus.FAILURE, resources));
  }

  public void updatePartitionSecretSuccess(List<String> resources) {
    writeLog(getAuditEvents().getUpdatePartitionSecretEvent(AuditStatus.SUCCESS, resources));
  }

  public void updatePartitionSecretFailure(List<String> resources) {
    writeLog(getAuditEvents().getUpdatePartitionSecretEvent(AuditStatus.FAILURE, resources));
  }

  public void readListPartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getListPartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void readListPartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getListPartitionEvent(AuditStatus.FAILURE, resources));
  }

  private void writeLog(AuditPayload log) {
    this.logger.audit(log);
  }
}
