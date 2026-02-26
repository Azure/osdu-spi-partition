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

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.Console;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class TestUtils {

	protected static String token = null;
	protected static String noAccessToken = null;

	public static String getApiPath(String api, boolean enforceHttp) throws MalformedURLException {
		String baseUrl = Config.Instance().hostUrl;
		if(enforceHttp)
			baseUrl = baseUrl.replaceFirst("https", "http");
		URL mergedURL = new URL(baseUrl + api);
		return mergedURL.toString();
	}

    public abstract String getAccessToken() throws Exception;

    public abstract String getNoAccessToken() throws Exception;

	public static Map<String, String> getOsduTenantHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("data-partition-id", Config.Instance().osduTenant);
		final String correlationId = UUID.randomUUID().toString();
		System.out.printf("Using correlation-id for the request: %s \n", correlationId);
		headers.put("correlation-id", correlationId);
		return headers;
	}

	public static Map<String, String> getCustomerTenantHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("data-partition-id", Config.Instance().clientTenant);

		final String correlationId = UUID.randomUUID().toString();
		System.out.printf("Using correlation-id for the request: %s \n", correlationId);
		headers.put("correlation-id", correlationId);

		return headers;
	}

	public static CloseableHttpResponse send(String path, String httpMethod, String token, String requestBody, String query, boolean enforceHttp)
			throws IOException {

		Map<String, String> headers = getOsduTenantHeaders();
		return send(path, httpMethod, token, requestBody, query, headers, enforceHttp);
	}

	public static CloseableHttpResponse send(String path, String httpMethod, String token, String requestBody, String query,
											 Map<String, String> headers, boolean enforceHttp)
			throws IOException {

		BasicHttpClientConnectionManager cm = createBasicHttpClientConnectionManager();
		ClassicHttpRequest httpRequest = createHttpRequest(path, httpMethod, token, requestBody, headers, enforceHttp);

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(cm).build()) {
			return httpClient.execute(httpRequest, new CustomHttpClientResponseHandler());
		}
	}

	/**
	 * Referenced for Test cases where [Token, body] not required. ex [Swagger API]
	 **/
	public static CloseableHttpResponse send(String path, String httpMethod, boolean enforceHttp) throws IOException {

		BasicHttpClientConnectionManager cm = createBasicHttpClientConnectionManager();
		ClassicHttpRequest httpRequest = createHttpRequest(path, httpMethod, enforceHttp);

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(cm).build()) {
			return httpClient.execute(httpRequest, new CustomHttpClientResponseHandler());
		}
	}

	private static ClassicHttpRequest createHttpRequest(String path, String httpMethod, String token, String requestBody,
														Map<String, String> headers, boolean enforceHttp) throws MalformedURLException {
		String url = getApiPath(path, enforceHttp);
		System.out.print("http req: " + url);
		ClassicRequestBuilder classicRequestBuilder = ClassicRequestBuilder.create(httpMethod)
				.setUri(url)
				.addHeader("Authorization", token)
				.setEntity(requestBody, ContentType.APPLICATION_JSON);
		headers.forEach(classicRequestBuilder::addHeader);
		return classicRequestBuilder.build();
	}

	private static ClassicHttpRequest createHttpRequest(String path, String httpMethod, boolean enforceHttp) throws MalformedURLException {
		String url = getApiPath(path, enforceHttp);
		System.out.print("http req: " + url);
		return ClassicRequestBuilder.create(httpMethod).setUri(url).build();
	}

	private static BasicHttpClientConnectionManager createBasicHttpClientConnectionManager() {
		ConnectionConfig connConfig = ConnectionConfig.custom()
				.setConnectTimeout(1500000, TimeUnit.MILLISECONDS)
				.setSocketTimeout(1500000, TimeUnit.MILLISECONDS)
				.build();
		BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
		cm.setConnectionConfig(connConfig);
		return cm;
	}

}
