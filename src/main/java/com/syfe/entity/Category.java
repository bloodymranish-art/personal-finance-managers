package com.syfe.financemanager.entity;

import com.syfe.financemanager.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    private boolean custom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}