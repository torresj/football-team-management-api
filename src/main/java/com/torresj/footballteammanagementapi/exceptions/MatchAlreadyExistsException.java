package com.torresj.footballteammanagementapi.exceptions;

public class MatchAlreadyExistsException extends Exception {
  public MatchAlreadyExistsException(String date) {
    super("Match for day  " + date + " already exists");
  }
}
