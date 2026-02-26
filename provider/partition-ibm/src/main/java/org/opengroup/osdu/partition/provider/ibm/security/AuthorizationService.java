/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.provider.ibm.security;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import lombok.extern.slf4j.Slf4j;

@Component
@RequestScope
@Slf4j
public class AuthorizationService implements IAuthorizationService {

	 @Autowired
	 DpsHeaders dpsheaders;
	 
	@Value("${service.partition.admin.user}")
	String partitionAdminUser;


	@Override
	public boolean isDomainAdminServiceAccount() {
		try {
			
			String userId = dpsheaders.getUserId();
			log.debug("logged in email : " + userId);
			if(userId != null  && partitionAdminUser != null && userId.equals(partitionAdminUser)) {
				return true;
			} else {
				throw AppException.createUnauthorized("Unauthorized. The user is not Service Principal");
			}
		
		}
		catch (AppException e) {
			throw e;
		}
		catch (Exception e) {
			throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Authentication Failure", e.getMessage(), e);
		}

	}

}

