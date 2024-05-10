package com.torresj.footballteammanagementapi.exceptions;

public class MemberBlockedException extends Exception {
  public MemberBlockedException(String username) {
    super("User " + username + " is blocked");
  }
}
