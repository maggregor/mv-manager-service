package com.achilio.mvm.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@NoArgsConstructor
public class UserProfile implements UserDetails {

  private String username;
  private String email;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty(value = "last_name")
  private String lastName;

  private String name;

  @JsonProperty(value = "customer_id")
  private String customerId;

  @JsonProperty("hd")
  private String teamName;

  @JsonProperty("roles")
  private Set<ERole> roles;

  @VisibleForTesting
  public UserProfile(
      String username,
      String email,
      String firstName,
      String lastName,
      String name,
      String teamName,
      String customerId,
      Set<ERole> roles) {
    this.username = username;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.name = name;
    this.teamName = teamName;
    this.customerId = customerId;
    this.roles = roles;
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

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (this.roles == null) {
      return null;
    }
    return this.roles.stream()
        .map(role -> new SimpleGrantedAuthority(role.name()))
        .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  @Override
  public boolean isAccountNonLocked() {
    return false;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }
}
