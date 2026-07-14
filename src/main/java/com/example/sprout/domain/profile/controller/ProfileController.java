package com.example.sprout.domain.profile.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.profile.dto.request.CreateProfileRequest;
import com.example.sprout.domain.profile.dto.response.CreateProfileResponse;
import com.example.sprout.domain.profile.dto.request.UpdateProfileRequest;
import com.example.sprout.domain.profile.dto.response.ProfileResponse;
import com.example.sprout.domain.profile.service.ProfileService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/me/profiles")
    public ResponseEntity<ApiResponse<CreateProfileResponse>> createProfile(@AuthMember Long memberId,
                                                                            @Valid @RequestBody CreateProfileRequest request) {

        CreateProfileResponse response = profileService.createProfile(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("프로필 생성 성공", response));
    }

    @GetMapping("/members/{memberId}/profiles")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@PathVariable("memberId") Long memberId,
                                                                   @AuthMember Long requesterId) {

        ProfileResponse response = profileService.getProfile(requesterId, memberId);
        return ResponseEntity.ok(ApiResponse.success("프로필 조회 성공", response));
    }

    @PatchMapping("/me/profiles")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@AuthMember Long requesterId,
                                                                      @Valid @RequestBody UpdateProfileRequest request) {

        log.info("프로필 수정 요청 - requesterId: {}", requesterId);
        ProfileResponse response = profileService.updateProfile(requesterId, request);
        return ResponseEntity.ok(ApiResponse.success("프로필 수정 성공", response));
    }
}
