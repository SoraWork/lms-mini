package com.hoaiphong.lmsmini.repository;

import com.hoaiphong.lmsmini.dto.CountDTO;
import com.hoaiphong.lmsmini.entity.Enrollment;
import com.hoaiphong.lmsmini.entity.EnrollmentId;
import com.hoaiphong.lmsmini.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.student.id IN :studentIds AND e.status = '1'")
    List<Enrollment> findByStudentIdIn(@Param("studentIds") List<Long> studentIds);
    // Count enrollments group by courseId, chỉ tính status = '1'
    @Query("SELECT new com.hoaiphong.lmsmini.dto.CountDTO(e.course.id, COUNT(e)) " +
            "FROM Enrollment e " +
            "WHERE e.course.id IN :courseIds AND e.status = '1' " +
            "GROUP BY e.course.id")
    List<CountDTO> countByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END
        FROM Enrollment e
        WHERE e.status = '1'
          AND e.id.studentId = :studentId
          AND e.id.courseId = :courseId
    """)
    boolean existsByStudentIdAndCourseIdAndActiveStatus(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId
    );
    @Query("""
    SELECT e FROM Enrollment e
    WHERE e.id.studentId = :studentId
      AND e.id.courseId = :courseId
    """)
    Enrollment findByStudentIdAndCourseId(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId
    );

    // --- xóa tất cả enrollment của student ---
    @Modifying
    @Query("UPDATE Enrollment e SET e.status = '0' WHERE e.student.id = :studentId AND e.status = '1'")
    int softDeleteByStudentId(@Param("studentId") Long studentId);

    // --- xóa tất cả enrollment của course ---
    @Modifying
    @Query("UPDATE Enrollment e SET e.status = '0' WHERE e.course.id = :courseId AND e.status = '1'")
    int softDeleteByCourseId(@Param("courseId") Long courseId);

    @Query("""
        SELECT DISTINCT e.student FROM Enrollment e
        WHERE e.course.id = :courseId 
            AND e.status = '1'
            AND e.student.status = '1'
            AND e.course.status = '1'
    """)
    Page<Student> findActiveStudentsByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    @Query("""
    SELECT COUNT(e)
    FROM Enrollment e
    WHERE e.student.id = :studentId
    AND e.status = '1'
""")
    Long countActiveByStudentId(@Param("studentId") Long studentId);

    @Query("""
    SELECT COUNT(e)
    FROM Enrollment e
    WHERE e.course.id = :courseId
    AND e.status = '1'
""")
    Long countActiveByCourseId(@Param("courseId") Long courseId);


}
