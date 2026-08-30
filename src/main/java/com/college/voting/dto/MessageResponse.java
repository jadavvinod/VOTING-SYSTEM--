package com.college.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;
    // Populated only in dev mode (no real SMTP configured) so the OTP flow is testable
    // end-to-end without email delivery. Remove/null this out once Spring Mail is wired
    // to actually send the OTP — see application.properties mail.* settings.
    private String devOnlyOtp;
}
