/*
  Copyright 2002-2021 Google LLC
  Copyright 2002-2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.opengroup.osdu.partition.logging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTest {

    private static final String TEST_USER = "partitionAccountUser";
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "TestAgent/1.0";
    private static final String TEST_AUTHORIZED_GROUP = "users.datalake.viewers";

    @Mock
    private JaxRsDpsLog log;

    @Mock
    private DpsHeaders headers;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuditLogger sut;

    private List<String> resources;
    private AuditEvents auditEvents;

    @Before
    public void setup() {
        resources = Collections.singletonList("resources");

        // Create reference AuditEvents with same values mocks will produce
        auditEvents = new AuditEvents(TEST_USER, TEST_IP, TEST_USER_AGENT, TEST_AUTHORIZED_GROUP);

        when(headers.getUserAuthorizedGroupName()).thenReturn(TEST_AUTHORIZED_GROUP);
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP);
        when(httpServletRequest.getHeader("user-agent")).thenReturn(TEST_USER_AGENT);
    }

    @Test
    public void should_writeCreatePartitionSuccessEvent() {
        sut.createPartitionSuccess(resources);

        verify(log).audit(auditEvents.getCreatePartitionEvent(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeCreatePartitionFailureEvent() {
        sut.createPartitionFailure(resources);

        verify(log).audit(auditEvents.getCreatePartitionEvent(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_writeReadPartitionSuccessEvent() {
        sut.readPartitionSuccess(resources);

        verify(log).audit(auditEvents.getReadPartitionEvent(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeReadPartitionFailureEvent() {
        sut.readPartitionFailure(resources);

        verify(log).audit(auditEvents.getReadPartitionEvent(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_writeDeletePartitionSuccessEvent() {
        sut.deletePartitionSuccess(resources);

        verify(log).audit(auditEvents.getDeletePartitionEvent(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeDeletePartitionFailureEvent() {
        sut.deletePartitionFailure(resources);

        verify(log).audit(auditEvents.getDeletePartitionEvent(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_writeReadServiceLivenessSuccessEvent() {
        sut.readServiceLivenessSuccess(resources);

        verify(log).audit(auditEvents.getReadServiceLivenessEvent(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeReadServiceLivenessFailureEvent() {
        sut.readServiceLivenessFailure(resources);

        verify(log).audit(auditEvents.getReadServiceLivenessEvent(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_writeUpdatePartitionSecretSuccessEvent() {
        sut.updatePartitionSecretSuccess(resources);

        verify(log).audit(auditEvents.getUpdatePartitionSecretEvent(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeUpdatePartitionSecretFailureEvent() {
        sut.updatePartitionSecretFailure(resources);

        verify(log).audit(auditEvents.getUpdatePartitionSecretEvent(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_writeReadListPartitionSuccessEvent() {
        sut.readListPartitionSuccess(resources);

        verify(log).audit(auditEvents.getListPartitionEvent(AuditStatus.SUCCESS, resources));
    }

    @Test
    public void should_writeReadListPartitionFailureEvent() {
        sut.readListPartitionFailure(resources);

        verify(log).audit(auditEvents.getListPartitionEvent(AuditStatus.FAILURE, resources));
    }

    @Test
    public void should_initializeAuditEvents_onlyOnce() {
        List<String> emptyResources = new ArrayList<>();
        sut.readListPartitionFailure(emptyResources);
        Object events1 = ReflectionTestUtils.getField(sut, "events");
        sut.readListPartitionFailure(resources);
        Object events2 = ReflectionTestUtils.getField(sut, "events");

        assertEquals(events1.hashCode(), events2.hashCode());
        verify(log).audit(auditEvents.getListPartitionEvent(AuditStatus.FAILURE, emptyResources));
        verify(log).audit(auditEvents.getListPartitionEvent(AuditStatus.FAILURE, resources));
    }
}
