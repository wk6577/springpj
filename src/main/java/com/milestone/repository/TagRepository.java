package com.milestone.repository;

import com.milestone.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // 태그명으로 태그 조회
    Optional<Tag> findByTagName(String tagName);

    // 태그명 존재 여부 확인
    boolean existsByTagName(String tagName);
}