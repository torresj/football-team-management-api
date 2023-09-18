package com.torresj.footballteammanagementapi.exceptions;

public class MatchNotFoundException extends Exception {
  public MatchNotFoundException(long id) {
    super("Match " + id + " not found");
  }
}
