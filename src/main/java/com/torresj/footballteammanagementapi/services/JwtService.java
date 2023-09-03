package com.torresj.footballteammanagementapi.services;

public interface JwtService {
	String createJWS(String name);
	String validateJWS(String jws);
}
