package com.achilio.mvm.service.events;

import com.google.api.client.util.ArrayMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Event {

  private final Map<String, String> data = new ArrayMap<>();
  private Type eventType;
  private String projectId;
  private String teamName;

  public Event(Type eventType) {
    this.eventType = eventType;
  }

  protected void addData(String key, String value) {
    this.data.put(key, value);
  }

  public enum Type {
    QUERY_FETCHER_JOB_STARTED,
    QUERY_FETCHER_JOB_FINISHED,
    DATA_MODEL_FETCHER_JOB_STARTED,
    DATA_MODEL_FETCHER_JOB_FINISHED,
  }

}
