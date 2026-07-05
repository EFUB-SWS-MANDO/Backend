package com.example.sprout.domain.profile.entity;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false, unique = true)
    private Member member;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "bio")
    private String bio;

    @Column(name = "sprout_level", nullable = false)
    private int sproutLevel;

    @Builder
    public Profile (Member member, String nickname, String profileImage, String bio) {
        this.member = member;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.bio = bio;
        //TODO: 새싹 초기 레벨 결정 필요 (0/1)
        this.sproutLevel = 1; //생성 시 기본값
    }

    public void updateProfile (String nickname, String profileImage, String bio) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.bio = bio;
    }

    //TODO: 레벨 업데이트 조건 추후 결정 필요
    public void updateSproutLevel() {
        this.sproutLevel += 1;
    }
}
