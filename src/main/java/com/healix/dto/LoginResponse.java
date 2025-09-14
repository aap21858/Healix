package com.healix.dto;

public record LoginResponse(String token, java.util.Set<String> role) {}

