package com.counselor.service.impl;

import com.counselor.dto.admin.BatchApproveRequest;
import com.counselor.dto.admin.BatchRequest;
import com.counselor.dto.admin.RejectRequest;
import com.counselor.entity.*;
import com.counselor.enums.*;
import com.counselor.repository.*;
import com.counselor.service.AdminService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BatchRepository batchRepository;
    private final AdminRepository adminRepository;
    private final SubmissionRepository submissionRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final EducationExperienceRepository educationExperienceRepository;
    private final CounselorRepository counselorRepository;
    private final ProfileEditRequestRepository profileEditRequestRepository;
    private final CollegeRepository collegeRepository;
    private final OperationLogRepository operationLogRepository;
    private final ObjectMapper objectMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.counselor.service.NotificationService notificationService;

    private void log(Long adminId, String action, String targetType, Long targetId, String detail) {
        operationLogRepository.save(OperationLog.builder()
            .admin(Admin.builder().id(adminId).build()).action(action)
            .targetType(targetType).targetId(targetId).detail(detail).build());
    }

    @Override public List<Batch> getBatches() { return batchRepository.findAll(Sort.by(Sort.Direction.DESC,"createdAt")); }

    @Override @Transactional public Batch createBatch(BatchRequest req) {
        Batch batch=batchRepository.save(Batch.builder().title(req.getTitle()).description(req.getDescription()).status(BatchStatus.COLLECTING).startTime(LocalDateTime.now()).build());
        notificationService.sendToActiveCounselors("新的信息采集批次", "“"+batch.getTitle()+"”已开始，请按时完成填报。", "BATCH", "/");
        return batch;
    }

    @Override @Transactional public Batch updateBatch(Long id, BatchRequest req) {
        Batch b = batchRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
        b.setTitle(req.getTitle()); b.setDescription(req.getDescription()); return batchRepository.save(b);
    }

    @Override @Transactional public void deleteBatch(Long id) {
        if (submissionRepository.findByBatchId(id).size() > 0) throw new RuntimeException("has submissions");
        batchRepository.deleteById(id);
    }

    @Override @Transactional public Batch toggleBatchStatus(Long id) {
        Batch b = batchRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
        if (b.getStatus() == BatchStatus.COLLECTING) { b.setStatus(BatchStatus.ENDED); b.setEndTime(LocalDateTime.now()); }
        else throw new RuntimeException("already ended");
        return batchRepository.save(b);
    }

    @Override public Map<String, Object> getBatchSubmissions(Long batchId, int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Submission> pageResult;
        if (StringUtils.hasText(status)) {
            SubmissionStatus ss = SubmissionStatus.valueOf(status.toUpperCase());
            List<Submission> list = submissionRepository.findByBatchIdAndStatus(batchId, ss);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("content", list.stream().map(s -> submissionToMap(s)).collect(Collectors.toList()));
            result.put("totalElements", list.size());
            return result;
        }
        pageResult = submissionRepository.findByBatchId(batchId, pageable);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", pageResult.getContent().stream().map(s -> submissionToMap(s)).collect(Collectors.toList()));
        result.put("totalElements", pageResult.getTotalElements());
        return result;
    }

    private Map<String, Object> submissionToMap(Submission s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("counselorId", s.getCounselor() != null ? s.getCounselor().getId() : null);
        map.put("counselorName", s.getCounselor() != null ? s.getCounselor().getName() : null);
        map.put("name", s.getName());
        map.put("gender", s.getGender());
        map.put("collegeName", s.getCollege() != null ? s.getCollege().getName() : null);
        map.put("status", s.getStatus().name());
        map.put("submittedAt", s.getSubmittedAt());
        map.put("reviewedAt", s.getReviewedAt());
        map.put("reviewComment", s.getReviewComment());
        map.put("createdAt", s.getCreatedAt());
        return map;
    }

    @Override public Map<String, Object> getSubmissionDetail(Long submissionId) {
        Submission s = submissionRepository.findById(submissionId).orElseThrow(() -> new RuntimeException("not found"));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("counselorName", s.getCounselor().getName());
        map.put("counselorUsername", s.getCounselor().getUsername());
        map.put("name", s.getName());
        map.put("gender", s.getGender());
        map.put("college", s.getCollege() != null ? s.getCollege().getName() : null);
        map.put("politicalStatus", s.getPoliticalStatus());
        map.put("highestEducation", s.getHighestEducation());
        map.put("officeAddress", s.getOfficeAddress());
        map.put("phone", s.getPhone());
        map.put("email", s.getEmail());
        map.put("status", s.getStatus().name());
        map.put("reviewComment", s.getReviewComment());
        map.put("submittedAt", s.getSubmittedAt());
        map.put("reviewedAt", s.getReviewedAt());
        map.put("createdAt", s.getCreatedAt());
        map.put("workExperiences", workExperienceRepository.findBySubmissionIdOrderBySortOrder(s.getId()).stream().map(we -> {
            Map<String, Object> wm = new LinkedHashMap<>();
            wm.put("id", we.getId()); wm.put("organization", we.getOrganization());
            wm.put("position", we.getPosition()); wm.put("startDate", we.getStartDate());
            wm.put("endDate", we.getEndDate()); wm.put("description", we.getDescription()); return wm;
        }).collect(Collectors.toList()));
        map.put("educationExperiences", educationExperienceRepository.findBySubmissionIdOrderBySortOrder(s.getId()).stream().map(ee -> {
            Map<String, Object> em = new LinkedHashMap<>();
            em.put("id", ee.getId()); em.put("school", ee.getSchool());
            em.put("major", ee.getMajor()); em.put("degree", ee.getDegree());
            em.put("startDate", ee.getStartDate()); em.put("endDate", ee.getEndDate()); return em;
        }).collect(Collectors.toList()));
        return map;
    }

    @Override @Transactional public void approveSubmissions(Long adminId, BatchApproveRequest req) {
        for (Long id : req.getIds()) {
            Submission s = submissionRepository.findById(id).orElseThrow(() -> new RuntimeException("not found: " + id));
            s.setStatus(SubmissionStatus.APPROVED); s.setReviewedAt(LocalDateTime.now()); submissionRepository.save(s);
            notificationService.send(s.getCounselor(),"信息填报审核通过","“"+s.getBatch().getTitle()+"”的信息填报已通过审核。","SUBMISSION","SUBMISSION",s.getId(),"/");
        }
        log(adminId, "batch approve", "submission", null, "approved " + req.getIds().size());
    }

    @Override @Transactional public void rejectSubmission(Long adminId, Long submissionId, RejectRequest req) {
        Submission s = submissionRepository.findById(submissionId).orElseThrow(() -> new RuntimeException("not found"));
        s.setStatus(SubmissionStatus.REJECTED); s.setReviewComment(req.getComment()); s.setReviewedAt(LocalDateTime.now());
        submissionRepository.save(s);
        notificationService.send(s.getCounselor(),"信息填报被驳回","“"+s.getBatch().getTitle()+"”需要修改。原因："+req.getComment(),"SUBMISSION","SUBMISSION",s.getId(),"/batch/"+s.getBatch().getId()+"/fill?submissionId="+s.getId());
        log(adminId, "reject submission", "submission", submissionId, req.getComment());
    }

    @Override public Page<Counselor> getCounselors(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Counselor> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String kw = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), kw), cb.like(cb.lower(root.get("username")), kw)));
            }
            if (StringUtils.hasText(status)) predicates.add(cb.equal(root.get("accountStatus"), AccountStatus.valueOf(status.toUpperCase())));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return counselorRepository.findAll(spec, pageable);
    }

    @Override public Map<String, Object> getCounselorDetail(Long id) {
        Counselor c = counselorRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId()); map.put("username", c.getUsername()); map.put("name", c.getName());
        map.put("gender", c.getGender());
        map.put("college", c.getCollege() != null ? c.getCollege().getName() : null);
        map.put("collegeId", c.getCollege() != null ? c.getCollege().getId() : null);
        map.put("politicalStatus", c.getPoliticalStatus());
        map.put("highestEducation", c.getHighestEducation());
        map.put("officeAddress", c.getOfficeAddress());
        map.put("phone", c.getPhone()); map.put("email", c.getEmail());
        map.put("accountStatus", c.getAccountStatus().name());
        map.put("createdAt", c.getCreatedAt());
        map.put("submissions", submissionRepository.findByCounselorIdOrderByCreatedAtDesc(id).stream().map(s -> {
            Map<String, Object> sm = new LinkedHashMap<>();
            sm.put("id", s.getId()); sm.put("batchTitle", s.getBatch().getTitle());
            sm.put("status", s.getStatus().name()); sm.put("submittedAt", s.getSubmittedAt()); return sm;
        }).collect(Collectors.toList()));
        return map;
    }

    @Override @Transactional public void approveRegistration(Long adminId, Long id) {
        Counselor c = counselorRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
        if (c.getAccountStatus() != AccountStatus.PENDING_REVIEW) throw new RuntimeException("not pending");
        c.setAccountStatus(AccountStatus.ACTIVE);c.setRegistrationReviewComment(null); counselorRepository.save(c);
        notificationService.send(c,"注册审核通过","您的辅导员账号已通过审核，现在可以登录系统。","REGISTRATION","COUNSELOR",c.getId(),"/");
        log(adminId, "approve registration", "counselor", id, c.getName());
    }

    @Override @Transactional public void rejectRegistration(Long adminId, Long id, RejectRequest req) {
        Counselor c = counselorRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
        if (c.getAccountStatus() != AccountStatus.PENDING_REVIEW) throw new RuntimeException("not pending");
        if(!StringUtils.hasText(req.getComment()))throw new RuntimeException("请填写驳回原因");
        c.setAccountStatus(AccountStatus.REJECTED);c.setRegistrationReviewComment(req.getComment()); counselorRepository.save(c);
        log(adminId, "驳回注册申请", "counselor", id, req.getComment());
    }

    @Override @Transactional public Counselor updateCounselor(Long adminId, Long id, Object req) {
        Counselor c = counselorRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
        if (!(req instanceof Map<?, ?> raw)) throw new RuntimeException("无效的修改内容");
        Map<String, Object> body = new LinkedHashMap<>();
        raw.forEach((key, value) -> body.put(String.valueOf(key), value));
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("name", c.getName()); before.put("gender", c.getGender()); before.put("collegeId", c.getCollege() == null ? null : c.getCollege().getId());
        before.put("politicalStatus", c.getPoliticalStatus()); before.put("highestEducation", c.getHighestEducation()); before.put("officeAddress", c.getOfficeAddress());
        before.put("phone", c.getPhone()); before.put("email", c.getEmail()); before.put("accountStatus", c.getAccountStatus());
        if (body.containsKey("name")) c.setName(Objects.toString(body.get("name"), "").trim());
        if (body.containsKey("gender")) c.setGender(Objects.toString(body.get("gender"), null));
        if (body.containsKey("collegeId")) c.setCollege(body.get("collegeId") == null ? null : collegeRepository.findById(Long.valueOf(body.get("collegeId").toString())).orElseThrow(() -> new RuntimeException("学院不存在")));
        if (body.containsKey("politicalStatus")) c.setPoliticalStatus(Objects.toString(body.get("politicalStatus"), null));
        if (body.containsKey("highestEducation")) c.setHighestEducation(Objects.toString(body.get("highestEducation"), null));
        if (body.containsKey("officeAddress")) c.setOfficeAddress(Objects.toString(body.get("officeAddress"), null));
        if (body.containsKey("phone")) c.setPhone(Objects.toString(body.get("phone"), null));
        if (body.containsKey("email")) c.setEmail(Objects.toString(body.get("email"), null));
        if (body.containsKey("photoUrl")) c.setPhotoUrl(Objects.toString(body.get("photoUrl"), null));
        if (body.containsKey("accountStatus")) c.setAccountStatus(AccountStatus.valueOf(body.get("accountStatus").toString()));
        if (!StringUtils.hasText(c.getName())) throw new RuntimeException("姓名不能为空");
        log(adminId, "修改辅导员资料", "counselor", id, "修改前=" + before + "，修改后=" + body);
        return counselorRepository.save(c);
    }

    @Override @Transactional public void disableCounselor(Long adminId, Long id) {
        Counselor c = counselorRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
        c.setAccountStatus(AccountStatus.DISABLED); counselorRepository.save(c);
        log(adminId, "disable counselor", "counselor", id, c.getName());
    }

    @Override public List<Map<String, Object>> getProfileEditRequests() {
        return profileEditRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(r -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("id", r.getId()); m.put("counselor", r.getCounselor() != null ? Map.of("id", r.getCounselor().getId(), "name", r.getCounselor().getName(), "photoUrl", r.getCounselor().getPhotoUrl() != null ? r.getCounselor().getPhotoUrl() : "") : null); m.put("changesJson", r.getChangesJson()); m.put("status", r.getStatus().name()); m.put("adminComment", r.getAdminComment()); m.put("createdAt", r.getCreatedAt()); m.put("updatedAt", r.getUpdatedAt()); return m; }).collect(Collectors.toList());
    }

    @Override @Transactional public void approveProfileEdit(Long adminId, Long requestId) {
        ProfileEditRequest req = profileEditRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("not found"));
        if (req.getStatus() != EditRequestStatus.PENDING) throw new RuntimeException("already processed");
        try {
            Map<String, Object> changes = objectMapper.readValue(req.getChangesJson(), new TypeReference<Map<String, Object>>() {});
            Counselor c = req.getCounselor();
            if (changes.containsKey("name")) c.setName((String) changes.get("name"));
            if (changes.containsKey("gender")) c.setGender((String) changes.get("gender"));
            if (changes.containsKey("collegeId")) c.setCollege(collegeRepository.findById(Long.valueOf(changes.get("collegeId").toString())).orElseThrow(() -> new RuntimeException("college not found")));
            if (changes.containsKey("politicalStatus")) c.setPoliticalStatus((String) changes.get("politicalStatus"));
            if (changes.containsKey("highestEducation")) c.setHighestEducation((String) changes.get("highestEducation"));
            if (changes.containsKey("officeAddress")) c.setOfficeAddress((String) changes.get("officeAddress"));
            if (changes.containsKey("phone")) c.setPhone((String) changes.get("phone"));
            if (changes.containsKey("email")) c.setEmail((String) changes.get("email"));
            if (changes.containsKey("photoUrl")) c.setPhotoUrl((String) changes.get("photoUrl"));
            counselorRepository.save(c);
            req.setStatus(EditRequestStatus.APPROVED); profileEditRequestRepository.save(req);
            notificationService.send(c,"资料修改审核通过","您的个人资料修改申请已通过。","PROFILE_EDIT","PROFILE_EDIT",requestId,"/profile");
            log(adminId, "approve profile edit", "profile_edit_request", requestId, c.getName());
        } catch (Exception e) { throw new RuntimeException("failed: " + e.getMessage()); }
    }

    @Override @Transactional public void rejectProfileEdit(Long adminId, Long requestId, RejectRequest req) {
        ProfileEditRequest r = profileEditRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("not found"));
        r.setStatus(EditRequestStatus.REJECTED); r.setAdminComment(req.getComment()); profileEditRequestRepository.save(r);
        notificationService.send(r.getCounselor(),"资料修改申请被驳回","原因："+req.getComment(),"PROFILE_EDIT","PROFILE_EDIT",requestId,"/profile");
        log(adminId, "reject profile edit", "profile_edit_request", requestId, req.getComment());
    }

    @Override public List<College> getColleges() { return collegeRepository.findAll(); }

    @Override @Transactional public College createCollege(String name) {
        if (collegeRepository.existsByName(name)) throw new RuntimeException("exists");
        return collegeRepository.save(College.builder().name(name).build());
    }

    @Override @Transactional public College updateCollege(Long id, String name) {
        College c = collegeRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
        c.setName(name); return collegeRepository.save(c);
    }

    @Override @Transactional public void deleteCollege(Long id) { collegeRepository.deleteById(id); }

    @Override public byte[] exportCounselors() {
        List<Counselor> list = counselorRepository.findAll();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Counselors");
            Row header = sheet.createRow(0);
            String[] h = {"Username","Name","Gender","College","Political","Education","Office","Phone","Email","Status"};
            for (int i = 0; i < h.length; i++) header.createCell(i).setCellValue(h[i]);
            int r = 1;
            for (Counselor c : list) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(c.getUsername()); row.createCell(1).setCellValue(c.getName());
                row.createCell(2).setCellValue(c.getGender());
                row.createCell(3).setCellValue(c.getCollege() != null ? c.getCollege().getName() : "");
                row.createCell(4).setCellValue(c.getPoliticalStatus()); row.createCell(5).setCellValue(c.getHighestEducation());
                row.createCell(6).setCellValue(c.getOfficeAddress()); row.createCell(7).setCellValue(c.getPhone());
                row.createCell(8).setCellValue(c.getEmail()); row.createCell(9).setCellValue(c.getAccountStatus().name());
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); wb.write(bos); return bos.toByteArray();
        } catch (Exception e) { throw new RuntimeException("export failed"); }
    }

    @Override public byte[] exportSubmissions(Long batchId) {
        List<Submission> list = submissionRepository.findByBatchId(batchId);
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Submissions");
            Row header = sheet.createRow(0);
            String[] h = {"Counselor","Name","Gender","College","Political","Education","Office","Phone","Email","Status"};
            for (int i = 0; i < h.length; i++) header.createCell(i).setCellValue(h[i]);
            int r = 1;
            for (Submission s : list) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(s.getCounselor().getName()); row.createCell(1).setCellValue(s.getName());
                row.createCell(2).setCellValue(s.getGender());
                row.createCell(3).setCellValue(s.getCollege() != null ? s.getCollege().getName() : "");
                row.createCell(4).setCellValue(s.getPoliticalStatus()); row.createCell(5).setCellValue(s.getHighestEducation());
                row.createCell(6).setCellValue(s.getOfficeAddress()); row.createCell(7).setCellValue(s.getPhone());
                row.createCell(8).setCellValue(s.getEmail()); row.createCell(9).setCellValue(s.getStatus().name());
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream(); wb.write(bos); return bos.toByteArray();
        } catch (Exception e) { throw new RuntimeException("export failed"); }
    }

    @Override public Map<String, Object> getLogs(int page, int size) {
        Page<OperationLog> pageResult = operationLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)); Map<String, Object> result = new LinkedHashMap<>(); result.put("content", pageResult.getContent().stream().map(l -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("id", l.getId()); m.put("admin", l.getAdmin() != null ? Map.of("id", l.getAdmin().getId(), "name", l.getAdmin().getName()) : null); m.put("action", l.getAction()); m.put("detail", l.getDetail()); m.put("createdAt", l.getCreatedAt()); return m; }).collect(Collectors.toList())); result.put("totalElements", pageResult.getTotalElements()); return result;
    }

    @Override public Map<String,Object> getBatchProgress(Long batchId){
        Batch batch=batchRepository.findById(batchId).orElseThrow(()->new RuntimeException("批次不存在"));
        List<Counselor> active=counselorRepository.findAll().stream().filter(c->c.getAccountStatus()==AccountStatus.ACTIVE).toList();
        List<Submission> records=submissionRepository.findByBatchId(batchId);
        Map<String,Object> result=new LinkedHashMap<>();long total=active.size(),participated=records.stream().map(s->s.getCounselor().getId()).distinct().count();
        result.put("batchTitle",batch.getTitle());result.put("total",total);result.put("participated",participated);result.put("unsubmitted",Math.max(0,total-participated));
        for(SubmissionStatus status:SubmissionStatus.values())result.put(status.name().toLowerCase(),records.stream().filter(s->s.getStatus()==status).count());
        result.put("completionRate",total==0?0:Math.round(participated*1000.0/total)/10.0);
        Map<String,Map<String,Object>> colleges=new LinkedHashMap<>();
        for(Counselor c:active){String name=c.getCollege()==null?"未设置学院":c.getCollege().getName();Map<String,Object> row=colleges.computeIfAbsent(name,k->{Map<String,Object>m=new LinkedHashMap<>();m.put("college",k);m.put("total",0L);m.put("participated",0L);return m;});row.put("total",(Long)row.get("total")+1);}
        Set<Long> ids=records.stream().map(s->s.getCounselor().getId()).collect(Collectors.toSet());for(Counselor c:active)if(ids.contains(c.getId())){String name=c.getCollege()==null?"未设置学院":c.getCollege().getName();Map<String,Object>row=colleges.get(name);row.put("participated",(Long)row.get("participated")+1);}
        colleges.values().forEach(row->{long t=(Long)row.get("total"),done=(Long)row.get("participated");row.put("rate",t==0?0:Math.round(done*1000.0/t)/10.0);});result.put("colleges",new ArrayList<>(colleges.values()));return result;
    }

    @Override public List<Map<String,Object>> getUnsubmitted(Long batchId,String keyword,Long collegeId){
        batchRepository.findById(batchId).orElseThrow(()->new RuntimeException("批次不存在"));Set<Long> submitted=submissionRepository.findByBatchId(batchId).stream().map(s->s.getCounselor().getId()).collect(Collectors.toSet());String search=keyword==null?"":keyword.trim().toLowerCase();
        return counselorRepository.findAll().stream().filter(c->c.getAccountStatus()==AccountStatus.ACTIVE&&!submitted.contains(c.getId())).filter(c->collegeId==null||(c.getCollege()!=null&&collegeId.equals(c.getCollege().getId()))).filter(c->search.isEmpty()||c.getName().toLowerCase().contains(search)||c.getUsername().toLowerCase().contains(search)).map(c->{Map<String,Object>m=new LinkedHashMap<>();m.put("id",c.getId());m.put("name",c.getName());m.put("username",c.getUsername());m.put("college",c.getCollege()==null?"未设置学院":c.getCollege().getName());m.put("phone",c.getPhone());return m;}).toList();
    }

    @Override @Transactional public int remindUnsubmitted(Long adminId,Long batchId,List<Long> counselorIds){Batch batch=batchRepository.findById(batchId).orElseThrow(()->new RuntimeException("批次不存在"));Set<Long> allowed=getUnsubmitted(batchId,null,null).stream().map(m->Long.valueOf(m.get("id").toString())).collect(Collectors.toSet());List<Long> targets=(counselorIds==null||counselorIds.isEmpty())?new ArrayList<>(allowed):counselorIds.stream().filter(allowed::contains).distinct().toList();for(Long id:targets)counselorRepository.findById(id).ifPresent(c->notificationService.send(c,"信息填报提醒","请尽快完成“"+batch.getTitle()+"”的信息填报。","REMINDER","BATCH",batchId,"/batch/"+batchId+"/fill"));log(adminId,"批次催报","batch",batchId,"发送"+targets.size()+"人");return targets.size();}

    @Override public byte[] exportUnsubmitted(Long batchId){List<Map<String,Object>> list=getUnsubmitted(batchId,null,null);try(Workbook wb=new XSSFWorkbook()){Sheet sheet=wb.createSheet("未填名单");String[] headers={"用户名","姓名","学院","电话"};Row header=sheet.createRow(0);for(int i=0;i<headers.length;i++)header.createCell(i).setCellValue(headers[i]);int r=1;for(Map<String,Object> item:list){Row row=sheet.createRow(r++);row.createCell(0).setCellValue(Objects.toString(item.get("username"),""));row.createCell(1).setCellValue(Objects.toString(item.get("name"),""));row.createCell(2).setCellValue(Objects.toString(item.get("college"),""));row.createCell(3).setCellValue(Objects.toString(item.get("phone"),""));}ByteArrayOutputStream out=new ByteArrayOutputStream();wb.write(out);return out.toByteArray();}catch(Exception e){throw new RuntimeException("导出失败");}}
    @Override @Transactional public String resetCounselorPassword(Long adminId,Long counselorId){Counselor c=counselorRepository.findById(counselorId).orElseThrow(()->new RuntimeException("辅导员不存在"));String temporary="Tmp"+UUID.randomUUID().toString().replace("-","").substring(0,8);c.setPassword(passwordEncoder.encode(temporary));c.setMustChangePassword(true);counselorRepository.save(c);log(adminId,"重置辅导员密码","counselor",counselorId,"已生成一次性临时密码");return temporary;}
    @Override @Transactional public void changeAdminPassword(Long adminId,com.counselor.dto.counselor.ChangePasswordRequest request){Admin admin=adminRepository.findById(adminId).orElseThrow(()->new RuntimeException("管理员不存在"));if(!passwordEncoder.matches(request.getOldPassword(),admin.getPassword()))throw new RuntimeException("原密码错误");if(request.getNewPassword()==null||request.getNewPassword().length()<8)throw new RuntimeException("新密码不能少于8位");admin.setPassword(passwordEncoder.encode(request.getNewPassword()));adminRepository.save(admin);log(adminId,"修改管理员密码","admin",adminId,"密码已修改");}
}
