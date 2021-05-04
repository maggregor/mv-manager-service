package com.alwaysmart.optimizer.controllers;

import com.alwaysmart.optimizer.services.OptimizerService;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.GregorianCalendar;

@RestController
@RequestMapping(path = "${v1API}")
@Validated
public class OptimizerController {

    private static Logger LOGGER = LoggerFactory.getLogger(OptimizerController.class);

    @PostMapping(path = "/optimize/{projectId}", produces = "application/json")
    public String optimizeProject(@RequestHeader("Authorization") String accessTokenString, @PathVariable String projectId) {
        return new OptimizerService(buildGoogleCredentials(accessTokenString)).optimizeProject(projectId);
    }

    private GoogleCredentials buildGoogleCredentials(String accessTokenString) {
        AccessToken accessToken = new AccessToken(accessTokenString, new GregorianCalendar(2021, Calendar.JUNE, 25, 5, 0).getTime());
        return UserCredentials.newBuilder()
                .setAccessToken(accessToken)
                .build();
    }

}
