package com.counselor.controller;

import com.counselor.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final CollegeRepository collegeRepository;

    @GetMapping("/colleges")
    public ResponseEntity<?> getColleges() {
        return ResponseEntity.ok(collegeRepository.findAll());
    }
}
