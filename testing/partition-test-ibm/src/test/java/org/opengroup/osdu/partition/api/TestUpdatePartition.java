/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.api;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.opengroup.osdu.partition.util.IBMTestUtils;

import static org.junit.Assert.assertEquals;

public class TestUpdatePartition extends UpdatePartitionTest {

    @Before
    @Override
    public void setup() {
        this.testUtils = new IBMTestUtils();
    }

    @After
    @Override
    public void tearDown() {
        this.testUtils = null;
    }
    
    //servicemesh changes response code - 403
    @Override
	public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
		 CloseableHttpResponse response = descriptor.run(getId(), "");
	     assertEquals(error(EntityUtils.toString(response.getEntity())), 403, response.getCode());
	}


}
