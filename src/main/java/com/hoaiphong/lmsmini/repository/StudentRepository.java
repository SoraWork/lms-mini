package com.hoaiphong.lmsmini.repository;

import com.hoaiphong.lmsmini.entity.Student;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("""
    SELECT s FROM Student s
    WHERE s.status = '1'
      AND (:id IS NULL OR s.id = :id)
""")
    Optional<Student> findByIdAndActiveStatus(@Param("id") Long id);
    @Query("""
        SELECT s FROM Student s
        WHERE s.status = '1'
            AND (:name IS NULL OR  LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\')
            AND (:email IS NULL OR  LOWER(s.email) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\')
    """)
    Page<Student> searchStudents(
            String name,
            String email,
            Pageable pageable
    );

}
