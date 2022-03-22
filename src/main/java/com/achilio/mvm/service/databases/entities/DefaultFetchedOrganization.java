package com.achilio.mvm.service.databases.entities;

public class DefaultFetchedOrganization implements FetchedOrganization {
  private final String id;
  private final String displayName;
  private final String workspaceId;

  public DefaultFetchedOrganization(
      final String name, final String displayName, final String workspaceId) {
    this.id = name;
    this.displayName = displayName;
    this.workspaceId = workspaceId;
  }

  /**
   * On GCP, organization name is a unique identifier that looks like this: organization/123456 We
   * rename the variable accordingly on our end, since we will use this as a primary key
   *
   * @return id
   */
  @Override
  public String getName() {
    return id;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String getDirectoryCustomerId() {
    return workspaceId;
  }

  @Override
  public String toString() {
    return this.displayName;
  }
}
