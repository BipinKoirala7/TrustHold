package com.trusthold.mail.events;

public record UserRegisteredEvent(String email, String role, String name) {}
