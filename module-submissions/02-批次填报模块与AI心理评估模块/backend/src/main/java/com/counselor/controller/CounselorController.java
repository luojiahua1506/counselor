package com.counselor.controller;

import com.counselor.dto.counselor.ChangePasswordRequest;
import com.counselor.dto.counselor.EditProfileRequest;
import com.counselor.dto.counselor.SubmissionRequest;
import com.counselor.security.JwtUtil;
import com.counselor.service.CounselorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/counselor")
@RequiredArgsConstructor
public class CounselorController {

    private final CounselorService counselorService;
    private final JwtUtil jwtUtil;
    private final com.counselor.service.NotificationService notificationService;

    private Long getCounselorId(HttpServletRequest request) {
        return jwtUtil.getUserIdFromToken(request.getHeader("Authorization").substring(7));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        return ResponseEntity.ok(counselorService.getProfile(getCounselorId(request)));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> submitProfileEdit(HttpServletRequest request, @RequestBody EditProfileRequest body) {
        counselorService.submitProfileEdit(getCounselorId(request), body);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequest body) {
        counselorService.changePassword(getCounselorId(request), body);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @GetMapping("/edit-requests")
    public ResponseEntity<?> getEditRequests(HttpServletRequest request) {
        return ResponseEntity.ok(counselorService.getEditRequests(getCounselorId(request)));
    }

    @GetMapping("/submissions/records")
    public ResponseEntity<?> getSubmissionRecords(HttpServletRequest request) {
        return ResponseEntity.ok(counselorService.getSubmissionRecords(getCounselorId(request)));
    }

    @GetMapping("/batches")
    public ResponseEntity<?> getAvailableBatches() {
        return ResponseEntity.ok(counselorService.getAvailableBatches());
    }

    @PostMapping("/submissions")
    public ResponseEntity<?> createSubmission(HttpServletRequest request, @RequestParam Long batchId, @RequestBody SubmissionRequest body) {
        return ResponseEntity.ok(counselorService.createSubmission(getCounselorId(request), batchId, body));
    }

    @PutMapping("/submissions/{id}")
    public ResponseEntity<?> updateSubmission(HttpServletRequest request, @PathVariable Long id, @RequestBody SubmissionRequest body) {
        return ResponseEntity.ok(counselorService.updateSubmission(getCounselorId(request), id, body));
    }

    @PostMapping("/submissions/{id}/submit")
    public ResponseEntity<?> submitSubmission(HttpServletRequest request, @PathVariable Long id) {
        counselorService.submitSubmission(getCounselorId(request), id);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<?> getSubmissionDetail(HttpServletRequest request, @PathVariable Long id) {
        return ResponseEntity.ok(counselorService.getSubmissionDetail(getCounselorId(request), id));
    }

    @GetMapping("/contacts")
    public ResponseEntity<?> getContacts(HttpServletRequest request) {
        return ResponseEntity.ok(counselorService.getContacts(getCounselorId(request)));
    }

    @GetMapping("/notifications") public ResponseEntity<?> notifications(HttpServletRequest request){return ResponseEntity.ok(notificationService.list(getCounselorId(request)));}
    @GetMapping("/notifications/unread-count") public ResponseEntity<?> unread(HttpServletRequest request){return ResponseEntity.ok(Map.of("count",notificationService.unread(getCounselorId(request))));}
    @PutMapping("/notifications/{id}/read") public ResponseEntity<?> read(HttpServletRequest request,@PathVariable Long id){notificationService.read(getCounselorId(request),id);return ResponseEntity.ok(Map.of("message","ok"));}
    @PutMapping("/notifications/read-all") public ResponseEntity<?> readAll(HttpServletRequest request){notificationService.readAll(getCounselorId(request));return ResponseEntity.ok(Map.of("message","ok"));}
}
