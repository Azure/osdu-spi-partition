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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opengroup.osdu.partition.service.PartitionServiceRole;

public class AuditOperationTest {

  @Test
  public void should_haveCorrectRolesForCreatePartition() {
    List<String> roles = AuditOperation.CREATE_PARTITION.getRequiredGroups();
    assertEquals(1, roles.size());
    assertTrue(roles.containsAll(Collections.singletonList(PartitionServiceRole.DOMAIN_ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForReadPartition() {
    List<String> roles = AuditOperation.READ_PARTITION.getRequiredGroups();
    assertEquals(1, roles.size());
    assertTrue(roles.containsAll(Collections.singletonList(PartitionServiceRole.DOMAIN_ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForDeletePartition() {
    List<String> roles = AuditOperation.DELETE_PARTITION.getRequiredGroups();
    assertEquals(1, roles.size());
    assertTrue(roles.containsAll(Collections.singletonList(PartitionServiceRole.DOMAIN_ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForReadServiceLiveness() {
    List<String> roles = AuditOperation.READ_SERVICE_LIVENESS.getRequiredGroups();
    assertEquals(1, roles.size());
    assertTrue(roles.containsAll(Collections.singletonList(PartitionServiceRole.DOMAIN_ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForUpdatePartition() {
    List<String> roles = AuditOperation.UPDATE_PARTITION.getRequiredGroups();
    assertEquals(1, roles.size());
    assertTrue(roles.containsAll(Collections.singletonList(PartitionServiceRole.DOMAIN_ADMIN)));
  }

  @Test
  public void should_haveCorrectRolesForReadListPartition() {
    List<String> roles = AuditOperation.READ_LIST_PARTITION.getRequiredGroups();
    assertEquals(1, roles.size());
    assertTrue(roles.containsAll(Collections.singletonList(PartitionServiceRole.DOMAIN_ADMIN)));
  }

  @Test
  public void should_returnUnmodifiableList() {
    List<String> roles = AuditOperation.CREATE_PARTITION.getRequiredGroups();
    assertNotNull(roles);
    try {
      roles.add("should-fail");
      assertTrue("Expected UnsupportedOperationException", false);
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test
  public void should_haveAllOperationsDefined() {
    for (AuditOperation op : AuditOperation.values()) {
      assertNotNull(op.getRequiredGroups());
      assertTrue(op.name() + " should have at least one required group", op.getRequiredGroups().size() > 0);
    }
  }
}
