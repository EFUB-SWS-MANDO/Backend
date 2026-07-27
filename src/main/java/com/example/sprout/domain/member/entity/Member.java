package com.example.sprout.domain.member.entity;

import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "members",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"oauth_provider", "oauth_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private OauthProvider oauthProvider;

    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    @Column(name = "last_visit_date")
    private LocalDateTime lastVisitDate;

    @Column(name = "visit_streak")
    private int visitStreak;

    @Builder
    public Member (String oauthId, OauthProvider oauthProvider) {
        this.oauthId = oauthId;
        this.oauthProvider = oauthProvider;
    }

    public void updateVisitStreak() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        if (lastVisitDate == null) {
            visitStreak = 1;
        } else {
            LocalDate lastDate = lastVisitDate.toLocalDate();

            if (lastDate.equals(today)) return;

            if (lastDate.plusDays(1).isEqual(today)) visitStreak++;
            else visitStreak = 1;
        }

        lastVisitDate = now;
    }

}
