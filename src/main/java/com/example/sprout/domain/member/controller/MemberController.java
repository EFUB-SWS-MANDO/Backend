package com.example.sprout.domain.member.controller;

import com.example.sprout.domain.member.service.MemberService;
import com.example.sprout.global.common.response.ApiResponse;
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
//    public ResponseEntity<ApiResponse<Void>> deleteMember(@AuthMember Long memberId) { :@AuthMember 사용 후 활성화
    public ResponseEntity<ApiResponse<Void>> deleteMember() {
        //TODO: 추후 @AuthMember 사용으로 수정 예정
        Long memberId = 1L; //@AuthMember 사용 전 임시 테스트용

        log.info("회원탈퇴 요청 - memberId: {}", memberId);

        memberService.deleteMember(memberId);

        return ResponseEntity.ok(ApiResponse.success("회원탈퇴 성공"));
    }
}
