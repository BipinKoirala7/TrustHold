package com.trusthold.mail.dtos;

public record UserRegisteredEvent(String email, String role, String name) {}
