/*
 * Copyright 2017-2020, Schlumberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.util;

import static org.junit.Assert.assertEquals;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.Test;

public abstract class BaseTestTemplate extends TestBase {

    protected RestDescriptor descriptor;

    public BaseTestTemplate(RestDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    protected abstract String getId();

    protected abstract int expectedOkResponseCode();

    protected String error(String body) {
        return String.format("%s: %s %s %s", descriptor.getHttpMethod(), descriptor.getPath(), descriptor.getQuery(), body);
    }

    @Test
    public void should_return20XResponseCode_when_makingValidHttpsRequest() throws Exception {
        should_return20X_when_usingCredentialsWithPermission(testUtils.getAccessToken());
    }

    @Test
    public void should_return400_when_makingHttpRequestWithoutValidUrl() throws Exception {
        CloseableHttpResponse response = descriptor.runWithInvalidPath(getId(), testUtils.getAccessToken());
        assertEquals(error(EntityUtils.toString(response.getEntity())), 400, response.getCode());
    }

    public void should_return20X_when_usingCredentialsWithPermission(String token) throws Exception {
        CloseableHttpResponse response = descriptor.run(getId(), token);
        assertEquals(error(response.getCode() == 204 ? "" : EntityUtils.toString(response.getEntity())), expectedOkResponseCode(), response.getCode());
    }
}
