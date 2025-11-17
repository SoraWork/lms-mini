package com.hoaiphong.lmsmini.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url; // URL của image/video

    @Column(nullable = false)
    private String type; // "IMAGE" hoặc "VIDEO"

//    @Column(nullable = false)
//    private String objectType; // "STUDENT", "COURSE", "LESSON"

    @Column(nullable = false)
    private Long objectId; // ID của object liên quan

    @Column(nullable = false, length = 1)
    private String status = "1"; // "1" active, "0" deleted

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}