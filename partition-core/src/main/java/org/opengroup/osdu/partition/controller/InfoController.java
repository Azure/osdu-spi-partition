package org.opengroup.osdu.partition.controller;

import org.opengroup.osdu.core.common.info.VersionInfoBuilder;
import org.opengroup.osdu.core.common.model.info.VersionInfo;
import org.opengroup.osdu.partition.api.InfoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class InfoController implements InfoApi {
    @Autowired
    private VersionInfoBuilder versionInfoBuilder;

    @Override
    public VersionInfo info() throws IOException {
        return versionInfoBuilder.buildVersionInfo();
    }
}
