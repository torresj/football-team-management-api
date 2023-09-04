package com.torresj.footballteammanagementapi.exceptions;

public class MemberAlreadyExistsException extends Exception {
  public MemberAlreadyExistsException(String username) {
    super("User " + username + " already exists");
  }
}
