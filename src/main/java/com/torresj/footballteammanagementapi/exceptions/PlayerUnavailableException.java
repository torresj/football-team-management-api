package com.torresj.footballteammanagementapi.exceptions;

public class PlayerUnavailableException extends Exception {
  public PlayerUnavailableException() {
    super("Player not available");
  }
}
