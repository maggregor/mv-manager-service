package com.achilio.mvm.service.databases.entities;

/** Represents a single project from database with metadata. */
public interface FetchedProject {

  /**
   * Return the project id.
   *
   * @return the project id.
   */
  String getProjectId();

  /**
   * Return the project name.
   *
   * @return the project name.
   */
  String getName();

  String getTeamName();
}
