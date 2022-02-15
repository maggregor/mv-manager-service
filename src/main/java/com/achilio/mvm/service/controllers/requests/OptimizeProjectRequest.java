package com.achilio.mvm.service.controllers.requests;

public class OptimizeProjectRequest {

  private int days;

  public OptimizeProjectRequest() {}

  public OptimizeProjectRequest(int days) {
    this.days = days;
  }

  public int getDays() {
    return days;
  }
}
