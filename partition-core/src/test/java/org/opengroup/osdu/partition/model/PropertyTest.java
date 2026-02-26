// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class PropertyTest {

    @Test
    public void defaultSensitive_shouldBeFalse() {
        Property property = new Property();
        assertFalse("Default sensitive should be false", property.isSensitive());
        assertNull("Default value should be null", property.getValue());
    }

    @Test
    public void builder_shouldRespectExplicitValues() {
        Property property = Property.builder()
                .sensitive(true)
                .value("testValue")
                .build();

        assertTrue(property.isSensitive());
        assertEquals("testValue", property.getValue());
    }

    @Test
    public void builder_shouldUseDefaultWhenNotSpecified() {
        Property property = Property.builder()
                .value("onlyValue")
                .build();

        // sensitive not set -> should default to false
        assertFalse(property.isSensitive());
        assertEquals("onlyValue", property.getValue());
    }
}
