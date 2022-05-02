package com.achilio.mvm.service.events;

import lombok.Getter;

@Getter
public abstract class Event {

  private final Type eventType;
  private final String projectId;
  private final String teamName;

  public Event(Type eventType, String teamName, String projectId) {
    this.eventType = eventType;
    this.teamName = teamName;
    this.projectId = projectId;
  }

  public abstract Object getData();

  public enum Type {
    QUERY_FETCHER_JOB_STARTED,
    QUERY_FETCHER_JOB_FINISHED
  }

}
