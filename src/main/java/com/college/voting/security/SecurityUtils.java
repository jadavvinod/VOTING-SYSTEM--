package com.college.voting.security;

import com.college.voting.entity.Admin;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /** Returns the current authenticated Admin, or null if the principal isn't an Admin. */
    public Admin getCurrentAdmin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Admin admin) {
            return admin;
        }
        return null;
    }
}
