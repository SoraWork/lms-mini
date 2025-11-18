package com.hoaiphong.lmsmini.repository;

import com.hoaiphong.lmsmini.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image,Long> {
    @Query("SELECT i FROM Image i WHERE i.objectId IN :objectIds AND i.status = '1'")
    List<Image> findByObjectIdsAndStatus(@Param("objectIds") List<Long> objectIds);

}
