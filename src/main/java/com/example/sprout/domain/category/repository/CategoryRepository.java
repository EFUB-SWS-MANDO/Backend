package com.example.sprout.domain.category.repository;

import com.example.sprout.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByTypeIn(List<String> types);
}
