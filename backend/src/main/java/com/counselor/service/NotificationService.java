package com.counselor.service;
import com.counselor.entity.*;import com.counselor.repository.*;import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.time.LocalDateTime;import java.util.*;
@Service @RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;private final CounselorRepository counselorRepository;
    public void send(Counselor counselor,String title,String content,String type,String businessType,Long businessId,String targetPath){repository.save(Notification.builder().counselor(counselor).title(title).content(content).type(type).businessType(businessType).businessId(businessId).targetPath(targetPath).build());}
    public void sendToActiveCounselors(String title,String content,String type,String targetPath){counselorRepository.findAll().stream().filter(c->c.getAccountStatus()==com.counselor.enums.AccountStatus.ACTIVE).forEach(c->send(c,title,content,type,null,null,targetPath));}
    public List<Map<String,Object>> list(Long counselorId){return repository.findByCounselorIdOrderByCreatedAtDesc(counselorId).stream().map(n->{Map<String,Object>m=new LinkedHashMap<>();m.put("id",n.getId());m.put("title",n.getTitle());m.put("content",n.getContent());m.put("type",n.getType());m.put("businessType",n.getBusinessType());m.put("businessId",n.getBusinessId());m.put("targetPath",n.getTargetPath());m.put("read",n.getRead());m.put("createdAt",n.getCreatedAt());return m;}).toList();}
    public long unread(Long counselorId){return repository.countByCounselorIdAndReadFalse(counselorId);}
    @Transactional public void read(Long counselorId,Long id){Notification n=repository.findById(id).orElseThrow(()->new RuntimeException("消息不存在"));if(!n.getCounselor().getId().equals(counselorId))throw new RuntimeException("无权访问");n.setRead(true);n.setReadAt(LocalDateTime.now());}
    @Transactional public void readAll(Long counselorId){repository.findByCounselorIdOrderByCreatedAtDesc(counselorId).stream().filter(n->!n.getRead()).forEach(n->{n.setRead(true);n.setReadAt(LocalDateTime.now());});}
}
