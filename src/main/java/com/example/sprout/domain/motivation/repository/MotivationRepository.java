package com.example.sprout.domain.motivation.repository;

import com.example.sprout.domain.motivation.entity.Motivation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MotivationRepository extends JpaRepository<Motivation, Long> {
    Optional<Motivation> findByDisplayOrder(int displayOrder);
}
