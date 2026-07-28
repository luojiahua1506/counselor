package com.counselor.service.impl;

import com.counselor.dto.auth.LoginRequest;
import com.counselor.dto.auth.LoginResponse;
import com.counselor.dto.auth.RegisterRequest;
import com.counselor.entity.Admin;
import com.counselor.entity.Counselor;
import com.counselor.entity.College;
import com.counselor.enums.AccountStatus;
import com.counselor.repository.AdminRepository;
import com.counselor.repository.CollegeRepository;
import com.counselor.repository.CounselorRepository;
import com.counselor.security.JwtUtil;
import com.counselor.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CounselorRepository counselorRepository;
    private final AdminRepository adminRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse counselorLogin(LoginRequest request) {
        Counselor counselor = counselorRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), counselor.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (counselor.getAccountStatus() == AccountStatus.PENDING_REVIEW) {
            throw new RuntimeException("账号尚未通过审核，请联系管理员");
        }
        if (counselor.getAccountStatus() == AccountStatus.DISABLED) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        if (counselor.getAccountStatus() == AccountStatus.REJECTED) {
            throw new RuntimeException("注册申请已被驳回，请在注册页面查询原因并重新提交");
        }
        String accessToken = jwtUtil.generateAccessToken(counselor.getUsername(), "ROLE_COUNSELOR", counselor.getId());
        String refreshToken = jwtUtil.generateRefreshToken(counselor.getUsername());
        return new LoginResponse(accessToken, refreshToken, "COUNSELOR", counselor.getId(), counselor.getName(),Boolean.TRUE.equals(counselor.getMustChangePassword()));
    }

    @Override
    public LoginResponse adminLogin(LoginRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String accessToken = jwtUtil.generateAccessToken(admin.getUsername(), "ROLE_ADMIN", admin.getId());
        String refreshToken = jwtUtil.generateRefreshToken(admin.getUsername());
        return new LoginResponse(accessToken, refreshToken, "ADMIN", admin.getId(), admin.getName());
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        Counselor counselor = counselorRepository.findByUsername(request.getUsername()).orElse(null);
        if (counselor != null && counselor.getAccountStatus() != AccountStatus.REJECTED) throw new RuntimeException("用户名已存在");
        if (counselor != null && !passwordEncoder.matches(request.getPassword(), counselor.getPassword())) throw new RuntimeException("原密码不正确，不能重新提交");
        if (counselor == null) counselor = new Counselor();
        counselor.setUsername(request.getUsername());
        counselor.setPassword(passwordEncoder.encode(request.getPassword()));
        counselor.setName(request.getName());
        counselor.setGender(request.getGender());
        if (request.getCollegeId() != null) {
            College college = collegeRepository.findById(request.getCollegeId())
                    .orElseThrow(() -> new RuntimeException("学院不存在"));
            counselor.setCollege(college);
        }
        counselor.setPoliticalStatus(request.getPoliticalStatus());
        counselor.setHighestEducation(request.getHighestEducation());
        counselor.setOfficeAddress(request.getOfficeAddress());
        counselor.setPhone(request.getPhone());
        counselor.setEmail(request.getEmail());
        counselor.setAccountStatus(AccountStatus.PENDING_REVIEW);
        counselor.setRegistrationReviewComment(null);
        counselorRepository.save(counselor);
    }

    @Override public java.util.Map<String,Object> registrationStatus(LoginRequest request){
        Counselor counselor=counselorRepository.findByUsername(request.getUsername()).orElseThrow(()->new RuntimeException("用户名或密码错误"));
        if(!passwordEncoder.matches(request.getPassword(),counselor.getPassword()))throw new RuntimeException("用户名或密码错误");
        java.util.Map<String,Object> result=new java.util.LinkedHashMap<>();result.put("status",counselor.getAccountStatus());result.put("comment",counselor.getRegistrationReviewComment());result.put("name",counselor.getName());return result;
    }
    @Override public LoginResponse refresh(com.counselor.dto.auth.RefreshTokenRequest request){String token=request.getRefreshToken();if(!jwtUtil.validateToken(token)||!jwtUtil.isRefreshToken(token))throw new RuntimeException("刷新令牌无效");String username=jwtUtil.getUsernameFromToken(token);if("ADMIN".equalsIgnoreCase(request.getRole())){Admin a=adminRepository.findByUsername(username).orElseThrow(()->new RuntimeException("账号不存在"));return new LoginResponse(jwtUtil.generateAccessToken(username,"ROLE_ADMIN",a.getId()),jwtUtil.generateRefreshToken(username),"ADMIN",a.getId(),a.getName());}Counselor c=counselorRepository.findByUsername(username).orElseThrow(()->new RuntimeException("账号不存在"));if(c.getAccountStatus()!=AccountStatus.ACTIVE)throw new RuntimeException("账号不可用");return new LoginResponse(jwtUtil.generateAccessToken(username,"ROLE_COUNSELOR",c.getId()),jwtUtil.generateRefreshToken(username),"COUNSELOR",c.getId(),c.getName(),Boolean.TRUE.equals(c.getMustChangePassword()));}
}
