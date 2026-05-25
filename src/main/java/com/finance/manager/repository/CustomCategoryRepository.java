package com.finance.manager.repository;

import com.finance.manager.entity.CustomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomCategoryRepository extends JpaRepository<CustomCategory, Long> {
    List<CustomCategory> findByUserId(Long userId);
    Optional<CustomCategory> findByNameIgnoreCaseAndUserId(String name, Long userId);
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
}
