package com.torresj.footballteammanagementapi.exceptions;

public class MovementNotFoundException extends Exception {
  public MovementNotFoundException(long id) {
    super("Movement " + id + " not found");
  }
}
