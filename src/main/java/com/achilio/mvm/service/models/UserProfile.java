package com.achilio.mvm.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfile {

  private String username;
  private String email;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty(value = "last_name")
  private String lastName;

  private String name;

  private String customerId;

  @JsonProperty("hd")
  private String teamName;

  @VisibleForTesting
  public UserProfile(
      String username,
      String email,
      String firstName,
      String lastName,
      String name,
      String teamName) {
    this.username = username;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.name = name;
    this.teamName = teamName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    UserProfile that = (UserProfile) o;

    if (!Objects.equals(username, that.username)) {
      return false;
    }
    if (!Objects.equals(email, that.email)) {
      return false;
    }
    if (!Objects.equals(firstName, that.firstName)) {
      return false;
    }
    if (!Objects.equals(lastName, that.lastName)) {
      return false;
    }
    if (!Objects.equals(name, that.name)) {
      return false;
    }
    return Objects.equals(teamName, that.teamName);
  }

  public String getCustomerId() {
    return this.customerId;
  }
}
