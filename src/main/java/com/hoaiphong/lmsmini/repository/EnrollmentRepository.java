package com.hoaiphong.lmsmini.repository;

import com.hoaiphong.lmsmini.dto.CountDTO;
import com.hoaiphong.lmsmini.entity.Enrollment;
import com.hoaiphong.lmsmini.entity.EnrollmentId;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
