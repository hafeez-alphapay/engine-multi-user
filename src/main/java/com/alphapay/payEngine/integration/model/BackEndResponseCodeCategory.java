package com.alphapay.payEngine.integration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "back_end_response_code_categories")
public class BackEndResponseCodeCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false, length = 255)
    private String categoryName;

    @Override
    public String toString() {
        return "BackEndResponseCodeCategory{" +
                "id=" + id +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}