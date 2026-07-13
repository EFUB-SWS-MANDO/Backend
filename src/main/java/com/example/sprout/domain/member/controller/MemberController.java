package com.example.sprout.domain.member.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.member.service.MemberService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteMember(@AuthMember Long memberId, HttpServletRequest request) {

        log.info("회원탈퇴 요청 - memberId: {}", memberId);
        memberService.deleteMember(memberId, request);

        return ResponseEntity.ok(ApiResponse.success("회원탈퇴 성공"));
    }
}
