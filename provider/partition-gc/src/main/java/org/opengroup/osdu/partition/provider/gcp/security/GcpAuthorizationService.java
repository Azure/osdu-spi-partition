/*
 Copyright 2002-2022 Google LLC
 Copyright 2002-2022 EPAM Systems, Inc

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

package org.opengroup.osdu.partition.provider.gcp.security;

import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Slf4j
@Component
@RequestScope
@ConditionalOnProperty(name = "environment", havingValue = "gcp")
public class GcpAuthorizationService implements IAuthorizationService {
  @Override
  public boolean isDomainAdminServiceAccount() {
    log.debug("Authorization/Authentication is on an infrastructure level.");
    return true;
  }
}
