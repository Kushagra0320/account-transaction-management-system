package com.atm.backend.util;

import java.util.UUID;

/**
 * Utility class for generating unique identifiers.
 * Centralizing UUID generation makes it easy to swap the strategy later
 * (e.g., to a distributed snowflake ID) without touching strategy classes.
 */
public final class IdGenerator {

    private IdGenerator() {
        // Prevent instantiation — static utility class
    }

    /** Generates a UUID-based transaction reference string. */
    public static String generateRef() {
        return UUID.randomUUID().toString();
    }
}
