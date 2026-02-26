// Copyright 2017-2020, Schlumberger
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
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PartitionInfoTest {

    @Test
    public void notNullWhenInitialized() {
        PartitionInfo p = new PartitionInfo();
        assertNotNull(p.getProperties());
    }

    @Test
    public void setProperties() {
        PartitionInfo p = new PartitionInfo();
        HashMap map = new HashMap();
        p.setProperties(map);
        assertEquals(map, p.getProperties());
    }

    @Test
    public void allArgsConstructor() {
        HashMap map = new HashMap();
        PartitionInfo p = new PartitionInfo(map);
        assertEquals(map, p.getProperties());
    }

    @Test
    public void builder() {
        HashMap map = new HashMap();
        PartitionInfo p = PartitionInfo.builder().properties(map).build();
        assertEquals(map, p.getProperties());
    }
}