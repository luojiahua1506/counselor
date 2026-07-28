package com.counselor.service.impl;

import com.counselor.dto.counselor.ChangePasswordRequest;
import com.counselor.dto.counselor.EditProfileRequest;
import com.counselor.dto.counselor.SubmissionRequest;
import com.counselor.entity.*;
import com.counselor.enums.*;
import com.counselor.repository.*;
import com.counselor.service.CounselorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorServiceImpl implements CounselorService {

    private final CounselorRepository counselorRepository;
    private final BatchRepository batchRepository;
    private final SubmissionRepository submissionRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final EducationExperienceRepository educationExperienceRepository;
    private final ProfileEditRequestRepository profileEditRequestRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    private Counselor getActiveCounselor(Long id) {
        Counselor c = counselorRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
        if (c.getAccountStatus() != AccountStatus.ACTIVE) throw new RuntimeException("account not active");
        return c;
    }

    @Override
    public Map<String, Object> getProfile(Long id) {
        return buildCounselorMap(getActiveCounselor(id));
    }

    @Override
    @Transactional
    public void submitProfileEdit(Long id, EditProfileRequest req) {
        Counselor c = getActiveCounselor(id);
        Map<String, Object> changes = new LinkedHashMap<>();
        if (req.getName() != null) changes.put("name", req.getName());
        if (req.getGender() != null) changes.put("gender", req.getGender());
        if (req.getCollegeId() != null) changes.put("collegeId", req.getCollegeId());
        if (req.getPoliticalStatus() != null) changes.put("politicalStatus", req.getPoliticalStatus());
        if (req.getHighestEducation() != null) changes.put("highestEducation", req.getHighestEducation());
        if (req.getOfficeAddress() != null) changes.put("officeAddress", req.getOfficeAddress());
        if (req.getPhone() != null) changes.put("phone", req.getPhone());
        if (req.getEmail() != null) changes.put("email", req.getEmail());
        if (req.getPhotoUrl() != null) changes.put("photoUrl", req.getPhotoUrl());        if (changes.isEmpty()) throw new RuntimeException("no changes");
        try {
            ProfileEditRequest er = ProfileEditRequest.builder()
                .counselor(c).changesJson(objectMapper.writeValueAsString(changes))
                .status(EditRequestStatus.PENDING).build();
            profileEditRequestRepository.save(er);
        } catch (Exception e) { throw new RuntimeException("submit failed"); }
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest req) {
        Counselor c = getActiveCounselor(id);
        if (!passwordEncoder.matches(req.getOldPassword(), c.getPassword())) throw new RuntimeException("wrong password");
        c.setPassword(passwordEncoder.encode(req.getNewPassword()));
        c.setMustChangePassword(false);
        counselorRepository.save(c);
    }

    @Override
    public List<Map<String, Object>> getEditRequests(Long id) {
        return profileEditRequestRepository.findByCounselorIdOrderByCreatedAtDesc(id).stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("changesJson", r.getChangesJson());
            m.put("status", r.getStatus().name());
            m.put("adminComment", r.getAdminComment());
            m.put("createdAt", r.getCreatedAt());
            m.put("updatedAt", r.getUpdatedAt());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getSubmissionRecords(Long id) {
        return submissionRepository.findByCounselorIdOrderByCreatedAtDesc(id).stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("batchId", s.getBatch().getId());
            m.put("batchTitle", s.getBatch().getTitle());
            m.put("name", s.getName());
            m.put("status", s.getStatus().name());
            m.put("submittedAt", s.getSubmittedAt());
            m.put("reviewedAt", s.getReviewedAt());
            m.put("reviewComment", s.getReviewComment());
            m.put("createdAt", s.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Batch> getAvailableBatches() {
        return batchRepository.findByStatusOrderByCreatedAtDesc(BatchStatus.COLLECTING);
    }

    @Override
    @Transactional
    public Submission createSubmission(Long cid, Long bid, SubmissionRequest req) {
        Counselor c = getActiveCounselor(cid);
        Batch b = batchRepository.findById(bid).orElseThrow(() -> new RuntimeException("batch not found"));
        if (b.getStatus() != BatchStatus.COLLECTING) throw new RuntimeException("batch not collecting");
        Optional<Submission> existing = submissionRepository.findByCounselorIdAndBatchId(cid, bid);
        if (existing.isPresent()) {
            Submission current = existing.get();
            if (current.getStatus() == SubmissionStatus.DRAFT || current.getStatus() == SubmissionStatus.REJECTED) {
                return updateSubmission(cid, current.getId(), req);
            }
            throw new RuntimeException("该批次已经提交，不能重复填报");
        }
        Submission s = new Submission();
        s.setCounselor(c); s.setBatch(b);
        fillSubmission(s, req);
        s.setStatus(SubmissionStatus.DRAFT);
        s = submissionRepository.save(s);
        saveExps(s, req);
        return s;
    }

    @Override
    @Transactional
    public Submission updateSubmission(Long cid, Long sid, SubmissionRequest req) {
        Submission s = submissionRepository.findById(sid).orElseThrow(() -> new RuntimeException("not found"));
        if (!s.getCounselor().getId().equals(cid)) throw new RuntimeException("forbidden");
        if (s.getStatus() != SubmissionStatus.DRAFT && s.getStatus() != SubmissionStatus.REJECTED) throw new RuntimeException("cannot edit");
        fillSubmission(s, req);
        s.setStatus(SubmissionStatus.DRAFT);
        s = submissionRepository.save(s);
        workExperienceRepository.deleteBySubmissionId(sid);
        educationExperienceRepository.deleteBySubmissionId(sid);
        saveExps(s, req);
        return s;
    }

    @Override
    @Transactional
    public void submitSubmission(Long cid, Long sid) {
        Submission s = submissionRepository.findById(sid).orElseThrow(() -> new RuntimeException("not found"));
        if (!s.getCounselor().getId().equals(cid)) throw new RuntimeException("forbidden");
        if (s.getStatus() != SubmissionStatus.DRAFT && s.getStatus() != SubmissionStatus.REJECTED) throw new RuntimeException("cannot submit");
        s.setStatus(SubmissionStatus.SUBMITTED);
        s.setSubmittedAt(LocalDateTime.now());
        submissionRepository.save(s);
    }

    @Override
    public Map<String, Object> getSubmissionDetail(Long cid, Long sid) {
        Submission s = submissionRepository.findById(sid).orElseThrow(() -> new RuntimeException("not found"));
        if (!s.getCounselor().getId().equals(cid)) throw new RuntimeException("forbidden");
        return buildSubmissionDetail(s);
    }

    @Override
    public List<Map<String, Object>> getContacts(Long cid) {
        return counselorRepository.findAll().stream()
            .filter(c -> !c.getId().equals(cid) && c.getAccountStatus() == AccountStatus.ACTIVE)
            .map(c -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", c.getId()); m.put("name", c.getName()); m.put("gender", c.getGender());
                m.put("college", c.getCollege() != null ? c.getCollege().getName() : null);
                m.put("politicalStatus", c.getPoliticalStatus());
                m.put("highestEducation", c.getHighestEducation());
                m.put("officeAddress", c.getOfficeAddress());
                m.put("phone", c.getPhone()); m.put("email", c.getEmail()); m.put("photoUrl", c.getPhotoUrl());
                return m;
            }).collect(Collectors.toList());
    }

    private void fillSubmission(Submission s, SubmissionRequest req) {
        s.setName(req.getName() != null ? req.getName() : s.getCounselor().getName());
        s.setGender(req.getGender() != null ? req.getGender() : s.getCounselor().getGender());
        s.setCollege(req.getCollegeId() != null ? collegeRepository.findById(req.getCollegeId()).orElse(null) : s.getCounselor().getCollege());
        s.setPoliticalStatus(req.getPoliticalStatus());
        s.setHighestEducation(req.getHighestEducation());
        s.setOfficeAddress(req.getOfficeAddress());
        s.setPhone(req.getPhone());
        s.setEmail(req.getEmail());
        s.setPhotoUrl(req.getPhotoUrl());
    }

    private void saveExps(Submission s, SubmissionRequest req) {
        if (req.getWorkExperiences() != null) {
            int i = 0;
            for (SubmissionRequest.ExperienceItem it : req.getWorkExperiences()) {
                WorkExperience we = WorkExperience.builder().submission(s)
                    .organization(it.getOrganization()).position(it.getPosition())
                    .startDate(it.getStartDate() != null ? LocalDate.parse(it.getStartDate()) : null)
                    .endDate(it.getEndDate() != null ? LocalDate.parse(it.getEndDate()) : null)
                    .description(it.getDescription())
                    .sortOrder(it.getSortOrder() != null ? it.getSortOrder() : i).build();
                workExperienceRepository.save(we); i++;
            }
        }
        if (req.getEducationExperiences() != null) {
            int i = 0;
            for (SubmissionRequest.ExperienceItem it : req.getEducationExperiences()) {
                EducationExperience ee = EducationExperience.builder().submission(s)
                    .school(it.getSchool()).major(it.getMajor()).degree(it.getDegree())
                    .startDate(it.getStartDate() != null ? LocalDate.parse(it.getStartDate()) : null)
                    .endDate(it.getEndDate() != null ? LocalDate.parse(it.getEndDate()) : null)
                    .sortOrder(it.getSortOrder() != null ? it.getSortOrder() : i).build();
                educationExperienceRepository.save(ee); i++;
            }
        }
    }

    private Map<String, Object> buildCounselorMap(Counselor c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId()); m.put("username", c.getUsername()); m.put("name", c.getName());
        m.put("gender", c.getGender());
        m.put("college", c.getCollege() != null ? c.getCollege().getName() : null);
        m.put("collegeId", c.getCollege() != null ? c.getCollege().getId() : null);
        m.put("politicalStatus", c.getPoliticalStatus());
        m.put("highestEducation", c.getHighestEducation());
        m.put("officeAddress", c.getOfficeAddress());
        m.put("phone", c.getPhone()); m.put("email", c.getEmail()); m.put("photoUrl", c.getPhotoUrl());
        m.put("photoUrl", c.getPhotoUrl());        m.put("accountStatus", c.getAccountStatus().name());
        return m;
    }

    private Map<String, Object> buildSubmissionDetail(Submission s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("batchTitle", s.getBatch().getTitle());
        m.put("batchId", s.getBatch().getId());
        m.put("name", s.getName()); m.put("gender", s.getGender());
        m.put("college", s.getCollege() != null ? s.getCollege().getName() : null);
        m.put("politicalStatus", s.getPoliticalStatus());
        m.put("highestEducation", s.getHighestEducation());
        m.put("officeAddress", s.getOfficeAddress());
        m.put("phone", s.getPhone()); m.put("email", s.getEmail());
        m.put("photoUrl", s.getPhotoUrl());
        m.put("status", s.getStatus().name());
        m.put("reviewComment", s.getReviewComment());
        m.put("submittedAt", s.getSubmittedAt());
        m.put("reviewedAt", s.getReviewedAt());
        m.put("createdAt", s.getCreatedAt());
        m.put("workExperiences", workExperienceRepository.findBySubmissionIdOrderBySortOrder(s.getId()).stream().map(we -> {
            Map<String, Object> wm = new LinkedHashMap<>();
            wm.put("id", we.getId()); wm.put("organization", we.getOrganization());
            wm.put("position", we.getPosition());
            wm.put("startDate", we.getStartDate()); wm.put("endDate", we.getEndDate());
            wm.put("description", we.getDescription()); return wm;
        }).collect(Collectors.toList()));
        m.put("educationExperiences", educationExperienceRepository.findBySubmissionIdOrderBySortOrder(s.getId()).stream().map(ee -> {
            Map<String, Object> em = new LinkedHashMap<>();
            em.put("id", ee.getId()); em.put("school", ee.getSchool());
            em.put("major", ee.getMajor()); em.put("degree", ee.getDegree());
            em.put("startDate", ee.getStartDate()); em.put("endDate", ee.getEndDate()); return em;
        }).collect(Collectors.toList()));
        return m;
    }
}
