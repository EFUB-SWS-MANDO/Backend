package com.example.sprout.domain.resume.repository;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.resume.entity.Resume;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    void deleteAllByAuthor (Member author);
    List<Resume> findAllByAuthor(Member member);

    @Query("""
            SELECT r FROM Resume r
            WHERE r.author = :author
            AND (:keyword IS NULL OR r.title ILIKE CONCAT('%', :keyword, '%'))
            AND (:idAfter IS NULL OR r.id < :idAfter)
            ORDER BY r.createdAt DESC
           """)
    List<Resume> findPageByAuthorAndKeyword(@Param("author") Member author,
                                            @Param("idAfter") Long idAfter,
                                            @Param("keyword") String keyword,
                                            Pageable pageable);

    @Query("""
            SELECT count(r) FROM Resume r
            WHERE r.author = :author
            AND (:keyword IS NULL OR r.title ILIKE CONCAT('%', :keyword, '%'))
           """)
    Long countAllByAuthorAndKeyword(@Param("author") Member author,
                                    @Param("keyword") String keyword);
}
