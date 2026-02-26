package org.opengroup.osdu.partition.logging;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;

public class AuditEventsTest {

    private static final String TEST_USER = "test@example.com";
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "TestAgent/1.0";
    private static final String TEST_AUTHORIZED_GROUP = "users.datalake.viewers";

    @Test
    public void shouldInitializeAuditEvents_whenUserIsValid() {
        AuditEvents auditEvents = new AuditEvents(TEST_USER, TEST_IP, TEST_USER_AGENT, TEST_AUTHORIZED_GROUP);

        assertNotNull(auditEvents);
    }

    @Test
    public void shouldInitializeAuditEvents_whenUserIsEmpty() {
        // Should not throw - uses "unknown" fallback
        AuditEvents auditEvents = new AuditEvents("", TEST_IP, TEST_USER_AGENT, TEST_AUTHORIZED_GROUP);
        List<String> resources = Collections.singletonList("test-resource");

        // Verify we can create audit events without exception
        var payload = auditEvents.getCreatePartitionEvent(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
    }

    @Test
    public void shouldInitializeAuditEvents_whenUserIsNull() {
        // Should not throw - uses "unknown" fallback
        AuditEvents auditEvents = new AuditEvents(null, TEST_IP, TEST_USER_AGENT, TEST_AUTHORIZED_GROUP);
        List<String> resources = Collections.singletonList("test-resource");

        // Verify we can create audit events without exception
        var payload = auditEvents.getCreatePartitionEvent(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
    }

    @Test
    public void shouldInitializeAuditEvents_whenOptionalFieldsAreNull() {
        // Should handle null optional fields gracefully
        AuditEvents auditEvents = new AuditEvents(TEST_USER, null, null, null);
        List<String> resources = Collections.singletonList("test-resource");

        // Verify we can create audit events without exception
        var payload = auditEvents.getCreatePartitionEvent(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
    }
}
