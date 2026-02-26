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
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.auth;

import static org.opengroup.osdu.partition.service.PartitionServiceRole.DOMAIN_ADMIN;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component("authorizationFilter")
@RequestScope
public class AuthorizationFilter {

    @Autowired
    private IAuthorizationService authorizationService;

    @Autowired
    private DpsHeaders headers;

    public boolean hasPermissions() {
        boolean isDomainAdmin = authorizationService.isDomainAdminServiceAccount();
        if (isDomainAdmin) {
            headers.put(DpsHeaders.USER_AUTHORIZED_GROUP_NAME, DOMAIN_ADMIN);
        }
        return isDomainAdmin;
    }
}
