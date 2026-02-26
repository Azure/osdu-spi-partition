package org.opengroup.osdu.partition.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;

public class HealthCheckControllerTests {

	private HealthCheckController sut;

	@Before
	public void setup() {
		this.sut = new HealthCheckController();
	}

	@Test
	public void should_returnHttp200_when_checkLiveness() {
		assertEquals(HttpStatus.OK, this.sut.livenessCheck().getStatusCode());
	}

}
