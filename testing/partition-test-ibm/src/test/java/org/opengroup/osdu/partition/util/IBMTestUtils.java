/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.util;

import org.opengroup.osdu.core.ibm.util.IdentityClient;

import com.google.common.base.Strings;

public class IBMTestUtils extends TestUtils {

	@Override
	public String getAccessToken() throws Exception {
		if(token == null || token.isEmpty()) {
			token=IdentityClient.getTokenForUserWithAccess();
		}
		return "Bearer " + token;
	}

	@Override
	public String getNoAccessToken() throws Exception {
		if(noAccessToken == null || noAccessToken.isEmpty()) {
			noAccessToken=IdentityClient.getTokenForUserWithNoAccess();
		}
		return "Bearer " + noAccessToken;
	}

}
