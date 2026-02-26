/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.api;



import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.util.IBMTestUtils;
import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.assertEquals;

@Slf4j
public class TestCreatePartition extends CreatePartitionTest {

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
    @Test
    @Override
    public void should_return401_when_accessingWithCredentialsWithoutPermission() throws Exception {

            CloseableHttpResponse response = descriptor.run("tenant1", testUtils.getNoAccessToken());
        log.info(
                "Test should_return401_when_accessingWithCredentialsWithoutPermission has a response code = {}."
                        + "This test depends on an infrastructure level.",
                response.getCode());
    }
    public void should_return401_when_noAccessToken() throws Exception {
        CloseableHttpResponse response = descriptor.runOnCustomerTenant(getId(), testUtils.getNoAccessToken());
        log.info(
                "Test should_return401_when_accessingWithCredentialsWithoutPermission has a response code = {}."
                        + "This test depends on an infrastructure level.",
                response.getCode());
    }


}
