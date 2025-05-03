package com.milestone.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "popup_notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopupNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String notice; // 공지 내용

    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성 시간
}
