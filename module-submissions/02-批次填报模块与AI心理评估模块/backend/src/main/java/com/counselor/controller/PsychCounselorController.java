package com.counselor.controller;

import com.counselor.dto.psych.PsychRecordRequest;
import com.counselor.security.JwtUtil;
import com.counselor.service.PsychAssessmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counselor/psych")
@RequiredArgsConstructor
public class PsychCounselorController {
    private final PsychAssessmentService service;
    private final JwtUtil jwtUtil;
    private Long userId(HttpServletRequest request) { return jwtUtil.getUserIdFromToken(request.getHeader("Authorization").substring(7)); }

    @GetMapping("/batches") public ResponseEntity<?> batches(HttpServletRequest request) { return ResponseEntity.ok(service.getCounselorBatches(userId(request))); }
    @GetMapping("/batches/{id}") public ResponseEntity<?> batch(HttpServletRequest request, @PathVariable Long id) { return ResponseEntity.ok(service.getBatchDetail(userId(request), id)); }
    @PostMapping("/records") public ResponseEntity<?> create(HttpServletRequest request, @Valid @RequestBody PsychRecordRequest body) { return ResponseEntity.ok(service.saveRecord(userId(request), body)); }
    @PutMapping("/records/{id}") public ResponseEntity<?> update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PsychRecordRequest body) { return ResponseEntity.ok(service.updateRecord(userId(request), id, body)); }
    @PostMapping("/records/{id}/submit") public ResponseEntity<?> submit(HttpServletRequest request, @PathVariable Long id) { return ResponseEntity.ok(service.submitRecord(userId(request), id)); }
    @GetMapping("/records") public ResponseEntity<?> records(HttpServletRequest request) { return ResponseEntity.ok(service.getRecords(userId(request))); }
    @GetMapping("/records/{id}/report") public ResponseEntity<?> report(HttpServletRequest request, @PathVariable Long id) { return ResponseEntity.ok(service.getReport(userId(request), id)); }
}
