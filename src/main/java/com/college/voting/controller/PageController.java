package com.college.voting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard() {
        return "student/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/elections")
    public String adminElections() {
        return "admin/elections";
    }

    @GetMapping("/student/elections")
    public String studentElections() {
        return "student/elections";
    }

    @GetMapping("/student/election-details")
    public String studentElectionDetails() {
        return "student/election-details";
    }

    @GetMapping("/student/vote")
    public String studentVote() {
        return "student/vote";
    }

    @GetMapping("/student/vote-success")
    public String studentVoteSuccess() {
        return "student/vote-success";
    }

    @GetMapping("/student/results")
    public String studentResults() {
        return "student/results";
    }

    @GetMapping("/student/notifications")
    public String studentNotifications() {
        return "student/notifications";
    }

    @GetMapping("/admin/results")
    public String adminResults() {
        return "admin/results";
    }

    @GetMapping("/admin/students")
    public String adminStudents() {
        return "admin/students";
    }

    @GetMapping("/admin/audit-logs")
    public String adminAuditLogs() {
        return "admin/audit-logs";
    }

    @GetMapping("/admin/candidates")
    public String adminCandidates() {
        return "admin/candidates";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "reset-password";
    }

    @GetMapping("/student/change-password")
    public String studentChangePassword() {
        return "student/change-password";
    }

    @GetMapping("/student/profile")
public String studentProfile() {
    return "student/profile";
}
}
