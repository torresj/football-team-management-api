package com.torresj.footballteammanagementapi.exceptions;

public class NextMatchException extends Exception {
  public NextMatchException() {
    super("No match created yet");
  }
}
