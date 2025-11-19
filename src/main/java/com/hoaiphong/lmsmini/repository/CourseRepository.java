package com.hoaiphong.lmsmini.repository;

import com.hoaiphong.lmsmini.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
    @Query("""
    SELECT c FROM Course c
    WHERE c.status = '1'
        AND (:id IS NULL OR c.id = :id)
    """)
    Optional<Course> findCourseByIdAndActiveStatus(@Param("id") Long id);

    @Query("""
    SELECT c FROM Course c
    WHERE c.status = '1'
         AND (:name IS NULL OR  LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\')
            AND (:code IS NULL OR  LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%')) ESCAPE '\\')
    """)
    Page<Course> searchCourses(
            String name,
            String code,
            Pageable pageable
    );

    @Query("SELECT c FROM Course c WHERE c.id = :id AND c.status = '1'")
    Optional<Course> findByIdAndActiveStatus(@Param("id") Long id);
}
