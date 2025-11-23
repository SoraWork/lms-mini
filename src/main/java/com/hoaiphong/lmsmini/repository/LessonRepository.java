package com.hoaiphong.lmsmini.repository;

import com.hoaiphong.lmsmini.dto.CountDTO;
import com.hoaiphong.lmsmini.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("""
       SELECT new com.hoaiphong.lmsmini.dto.CountDTO(l.course.id, COUNT(l))
       FROM Lesson l
       WHERE l.course.id IN :courseIds AND l.status = '1'
       GROUP BY l.course.id
       """)
    List<CountDTO> countByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("""
       SELECT l 
       FROM Lesson l 
       JOIN FETCH l.course 
       WHERE l.course.id = :courseId AND l.status = '1'
       """)
    List<Lesson> findByCourseIdAndStatus(@Param("courseId") Long courseId);

    @Query("""
   SELECT l
   FROM Lesson l
   WHERE l.course.id = :courseId AND l.status = '1'
""")
    List<Lesson> findByCourseIdActive(@Param("courseId") Long courseId);

    @Query("""
    SELECT ls FROM Lesson ls
    WHERE ls.status = '1'
        AND (:id IS NULL OR ls.id = :id)
    """)
    Optional<Lesson> findLessonByIdAndActiveStatus(@Param("id") Long id);

    @Query("""
    SELECT COUNT(l) 
    FROM Lesson l 
    WHERE l.course.id = :courseId 
    AND l.status = '1'
    """)
    Long countActiveByCourseId(@Param("courseId") Long courseId);
}