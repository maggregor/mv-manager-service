package com.achilio.mvm.service.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryStatisticsResponse {

    public long totalSelect;
    public long totalSelectCaught;
    public long totalScanned;
    public long totalScannedCaught;

    public QueryStatisticsResponse(
            final long totalSelect,
            final long totalSelectCaught,
            final long totalScanned,
            final long totalScannedCaught) {
        this.totalSelect = totalSelect;
        this.totalSelectCaught = totalSelectCaught;
        this.totalScanned = totalScanned;
        this.totalScannedCaught = totalScannedCaught;
    }
}
