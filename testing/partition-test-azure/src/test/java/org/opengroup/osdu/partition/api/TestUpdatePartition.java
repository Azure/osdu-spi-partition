package org.opengroup.osdu.partition.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.util.AzureTestUtils;

public class TestUpdatePartition extends UpdatePartitionTest {

    @Before
    @Override
    public void setup() {
        this.testUtils = new AzureTestUtils();
    }

    @After
    @Override
    public void tearDown() {
        this.testUtils = null;
    }

    @Test
    @Override
    public void should_return401_when_noAccessToken() throws Exception {
        // revisit this later -- Istio is changing the response code
    }

    @Test
    @Override
    public void should_return401_when_accessingWithCredentialsWithoutPermission() throws Exception {
        // revisit this later -- Istio is changing the response code
    }

    @Test
    @Override
    public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
        // revisit this later -- Istio is changing the response code
    }
}
