package com.achilio.mvm.service.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class    RootController {

    @Value("${application.name}")
    private String applicationName;

    @Value("${build.version}")
    private String version;

    @GetMapping(path = "/")
    @ApiOperation("Get the build information")
    public Map<String, String> version() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("application-name", applicationName);
        m.put("build-version", version);
        return m;
    }
}
