package org.opengroup.osdu.partition.coreplus.security;

import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Slf4j
@Component
@RequestScope
public class AuthorizationService implements IAuthorizationService {
    @Override
    public boolean isDomainAdminServiceAccount() {
        //default implementation
        return true;
    }
}
