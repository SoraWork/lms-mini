package com.hoaiphong.lmsmini.repository;

import com.hoaiphong.lmsmini.entity.Enrollment;
import com.hoaiphong.lmsmini.entity.EnrollmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.student.id IN :studentIds AND e.status = '1'")
    List<Enrollment> findByStudentIdIn(@Param("studentIds") List<Long> studentIds);
}
