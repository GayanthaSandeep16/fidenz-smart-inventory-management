package com.example.fidenz.dto;

public record AuthResponse(
    String token,
    String username,
    String role
) {}
