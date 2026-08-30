package com.college.voting.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter for login attempts. Resets on app restart and isn't
 * shared across multiple app instances — fine for a single-instance college project.
 * For production/multi-instance deployment, back this with Redis instead.
 */
@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MINUTES = 15;

    private record Attempts(int count, LocalDateTime lockedUntil) {}

    private final ConcurrentHashMap<String, Attempts> attemptsByEmail = new ConcurrentHashMap<>();

    public void recordFailure(String email) {
        attemptsByEmail.compute(email, (key, current) -> {
            int newCount = (current == null ? 0 : current.count()) + 1;
            LocalDateTime lockedUntil = newCount >= MAX_ATTEMPTS
                    ? LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES)
                    : null;
            return new Attempts(newCount, lockedUntil);
        });
    }

    public void recordSuccess(String email) {
        attemptsByEmail.remove(email);
    }

    public boolean isLocked(String email) {
        Attempts attempts = attemptsByEmail.get(email);
        if (attempts == null || attempts.lockedUntil() == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(attempts.lockedUntil())) {
            attemptsByEmail.remove(email); // lockout expired
            return false;
        }
        return true;
    }

    public long minutesUntilUnlock(String email) {
        Attempts attempts = attemptsByEmail.get(email);
        if (attempts == null || attempts.lockedUntil() == null) return 0;
        long minutes = java.time.temporal.ChronoUnit.MINUTES.between(LocalDateTime.now(), attempts.lockedUntil());
        return Math.max(minutes, 1);
    }
}
