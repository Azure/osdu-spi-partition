/*
 * Copyright 2017-2025, The Open Group
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

package org.opengroup.osdu.partition.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.info.VersionInfoBuilder;
import org.opengroup.osdu.core.common.model.info.VersionInfo;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class InfoControllerTest {
    @InjectMocks
    private InfoController sut;

    @Mock
    private VersionInfoBuilder versionInfoBuilder;

    @Test
    public void should_return200_getVersionInfo() throws IOException {
        VersionInfo expectedVersionInfo = VersionInfo.builder()
                .groupId("group")
                .artifactId("artifact")
                .version("0.1.0")
                .buildTime("1000")
                .branch("master")
                .commitId("7777")
                .commitMessage("Test commit")
                .build();
        when(versionInfoBuilder.buildVersionInfo()).thenReturn(expectedVersionInfo);

        VersionInfo actualVersionInfo = this.sut.info();

        assertEquals(expectedVersionInfo, actualVersionInfo);
    }
}
