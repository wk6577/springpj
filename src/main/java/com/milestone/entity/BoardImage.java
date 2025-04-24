package com.milestone.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "board_image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_image_no")
    private Long boardImageNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_no", nullable = false)
    private Board board;

    @Column(name = "board_image_name", nullable = false, length = 1000)
    private String boardImageName;

    @Column(name = "board_image_path", nullable = false, length = 1000)
    private String boardImagePath;

    @Column(name = "board_image_order", nullable = false)
    private Integer boardImageOrder;

    @CreationTimestamp
    @Column(name = "board_image_inputdate", nullable = false, updatable = false)
    private LocalDateTime boardImageInputdate;
}