package com.hoaiphong.lmsmini.repository;

import com.hoaiphong.lmsmini.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    @Query(
        value = """
        SELECT s FROM Student s
        WHERE s.status = '1'
            AND (:name IS NULL OR  LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\')
            AND (:email IS NULL OR  LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')) ESCAPE '\\')
    """,
    countQuery = """
            SELECT COUNT(s) FROM Student s
            WHERE s.status = '1'
            AND (:name IS NULL OR  LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\')
            AND (:email IS NULL OR  LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')) ESCAPE '\\')
    """)
    Page<Student> searchStudents(
            String name,
            String email,
            Pageable pageable
    );
    // tách phân trang làm 2 câu
    @Query("""
        SELECT s FROM Student s
        WHERE s.status = '1'
            AND (:name IS NULL OR  LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\')
            AND (:email IS NULL OR  LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')) ESCAPE '\\')
    """)
    List<Student> searchStudentsList(
            String name,
            String email,
            Pageable pageable
    );
    @Query("""
        SELECT COUNT(s) FROM Student s
        WHERE s.status = '1'
            AND (:name IS NULL OR  LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) ESCAPE '\\')
            AND (:email IS NULL OR  LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')) ESCAPE '\\')
    """)
    Long countStudents(
            String name,
            String email
    );
    @Query("SELECT s FROM Student s WHERE s.status = '1'")
    List<Student> findAllActiveStudents();
}
