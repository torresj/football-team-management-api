package com.torresj.footballteammanagementapi.exceptions;

public class MemberNotFoundException extends Exception {
  public MemberNotFoundException(String username) {
    super("User " + username + " not found");
  }
}
