package com.healix.dto;

public record SetPasswordRequest(String token, String password) {
}
