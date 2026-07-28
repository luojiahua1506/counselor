package com.counselor.controller;

import com.counselor.dto.admin.BatchApproveRequest;
import com.counselor.dto.admin.BatchRequest;
import com.counselor.dto.admin.RejectRequest;
import com.counselor.security.JwtUtil;
import com.counselor.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    private Long getAdminId(HttpServletRequest request) {
        return jwtUtil.getUserIdFromToken(request.getHeader("Authorization").substring(7));
    }

    @GetMapping("/batches")
    public ResponseEntity<?> getBatches() { return ResponseEntity.ok(adminService.getBatches()); }

    @PostMapping("/batches")
    public ResponseEntity<?> createBatch(@RequestBody BatchRequest req) { return ResponseEntity.ok(adminService.createBatch(req)); }

    @PutMapping("/batches/{id}")
    public ResponseEntity<?> updateBatch(@PathVariable Long id, @RequestBody BatchRequest req) { return ResponseEntity.ok(adminService.updateBatch(id, req)); }

    @DeleteMapping("/batches/{id}")
    public ResponseEntity<?> deleteBatch(@PathVariable Long id) { adminService.deleteBatch(id); return ResponseEntity.ok(Map.of("message","ok")); }

    @PutMapping("/batches/{id}/status")
    public ResponseEntity<?> toggleBatchStatus(@PathVariable Long id) { return ResponseEntity.ok(adminService.toggleBatchStatus(id)); }

    @GetMapping("/batches/{batchId}/submissions")
    public ResponseEntity<?> getBatchSubmissions(@PathVariable Long batchId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size, @RequestParam(required=false) String status) {
        return ResponseEntity.ok(adminService.getBatchSubmissions(batchId, page, size, status));
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<?> getSubmissionDetail(@PathVariable Long id) { return ResponseEntity.ok(adminService.getSubmissionDetail(id)); }

    @PostMapping("/submissions/approve")
    public ResponseEntity<?> approveSubmissions(HttpServletRequest req, @RequestBody BatchApproveRequest body) {
        adminService.approveSubmissions(getAdminId(req), body);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @PostMapping("/submissions/{id}/reject")
    public ResponseEntity<?> rejectSubmission(HttpServletRequest req, @PathVariable Long id, @RequestBody RejectRequest body) {
        adminService.rejectSubmission(getAdminId(req), id, body);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @GetMapping("/counselors")
    public ResponseEntity<?> getCounselors(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size, @RequestParam(required=false) String keyword, @RequestParam(required=false) String status) {
        return ResponseEntity.ok(adminService.getCounselors(page, size, keyword, status));
    }

    @GetMapping("/counselors/{id}")
    public ResponseEntity<?> getCounselorDetail(@PathVariable Long id) { return ResponseEntity.ok(adminService.getCounselorDetail(id)); }

    @PostMapping("/registrations/{id}/approve")
    public ResponseEntity<?> approveRegistration(HttpServletRequest req, @PathVariable Long id) {
        adminService.approveRegistration(getAdminId(req), id);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @PostMapping("/registrations/{id}/reject")
    public ResponseEntity<?> rejectRegistration(HttpServletRequest req, @PathVariable Long id, @RequestBody RejectRequest body) {
        adminService.rejectRegistration(getAdminId(req), id, body);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @PutMapping("/counselors/{id}")
    public ResponseEntity<?> updateCounselor(HttpServletRequest req, @PathVariable Long id, @RequestBody Map<String,Object> body) {
        return ResponseEntity.ok(adminService.updateCounselor(getAdminId(req), id, body));
    }

    @DeleteMapping("/counselors/{id}")
    public ResponseEntity<?> disableCounselor(HttpServletRequest req, @PathVariable Long id) {
        adminService.disableCounselor(getAdminId(req), id);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @GetMapping("/profile-edit-requests")
    public ResponseEntity<?> getProfileEditRequests() { return ResponseEntity.ok(adminService.getProfileEditRequests()); }

    @PostMapping("/profile-edit-requests/{id}/approve")
    public ResponseEntity<?> approveProfileEdit(HttpServletRequest req, @PathVariable Long id) {
        adminService.approveProfileEdit(getAdminId(req), id);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @PostMapping("/profile-edit-requests/{id}/reject")
    public ResponseEntity<?> rejectProfileEdit(HttpServletRequest req, @PathVariable Long id, @RequestBody RejectRequest body) {
        adminService.rejectProfileEdit(getAdminId(req), id, body);
        return ResponseEntity.ok(Map.of("message","ok"));
    }

    @GetMapping("/colleges")
    public ResponseEntity<?> getColleges() { return ResponseEntity.ok(adminService.getColleges()); }

    @PostMapping("/colleges")
    public ResponseEntity<?> createCollege(@RequestBody Map<String,String> body) { return ResponseEntity.ok(adminService.createCollege(body.get("name"))); }

    @PutMapping("/colleges/{id}")
    public ResponseEntity<?> updateCollege(@PathVariable Long id, @RequestBody Map<String,String> body) { return ResponseEntity.ok(adminService.updateCollege(id, body.get("name"))); }

    @DeleteMapping("/colleges/{id}")
    public ResponseEntity<?> deleteCollege(@PathVariable Long id) { adminService.deleteCollege(id); return ResponseEntity.ok(Map.of("message","ok")); }

    @GetMapping("/export/counselors")
    public ResponseEntity<byte[]> exportCounselors() {
        byte[] data = adminService.exportCounselors();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=counselors.xlsx").contentType(MediaType.APPLICATION_OCTET_STREAM).body(data);
    }

    @GetMapping("/export/submissions/{batchId}")
    public ResponseEntity<byte[]> exportSubmissions(@PathVariable Long batchId) {
        byte[] data = adminService.exportSubmissions(batchId);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=submissions.xlsx").contentType(MediaType.APPLICATION_OCTET_STREAM).body(data);
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(adminService.getLogs(page, size));
    }

    @GetMapping("/batches/{id}/progress") public ResponseEntity<?> progress(@PathVariable Long id){return ResponseEntity.ok(adminService.getBatchProgress(id));}
    @GetMapping("/batches/{id}/unsubmitted") public ResponseEntity<?> unsubmitted(@PathVariable Long id,@RequestParam(required=false)String keyword,@RequestParam(required=false)Long collegeId){return ResponseEntity.ok(adminService.getUnsubmitted(id,keyword,collegeId));}
    @PostMapping("/batches/{id}/remind") public ResponseEntity<?> remind(HttpServletRequest request,@PathVariable Long id,@RequestBody(required=false)Map<String,Object> body){List<Long> ids=new java.util.ArrayList<>();if(body!=null&&body.get("counselorIds") instanceof List<?> list)list.forEach(value->ids.add(Long.valueOf(value.toString())));return ResponseEntity.ok(Map.of("count",adminService.remindUnsubmitted(getAdminId(request),id,ids)));}
    @GetMapping("/export/unsubmitted/{batchId}") public ResponseEntity<byte[]> exportUnsubmitted(@PathVariable Long batchId){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=unsubmitted.xlsx").contentType(MediaType.APPLICATION_OCTET_STREAM).body(adminService.exportUnsubmitted(batchId));}
    @PostMapping("/counselors/{id}/reset-password") public ResponseEntity<?> resetPassword(HttpServletRequest request,@PathVariable Long id){return ResponseEntity.ok(Map.of("temporaryPassword",adminService.resetCounselorPassword(getAdminId(request),id)));}
    @PutMapping("/password") public ResponseEntity<?> changeAdminPassword(HttpServletRequest request,@RequestBody com.counselor.dto.counselor.ChangePasswordRequest body){adminService.changeAdminPassword(getAdminId(request),body);return ResponseEntity.ok(Map.of("message","ok"));}
}
