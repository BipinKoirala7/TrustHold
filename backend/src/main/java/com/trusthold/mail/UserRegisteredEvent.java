package com.trusthold.mail;

public record UserRegisteredEvent(String email, String role, String name) {}
