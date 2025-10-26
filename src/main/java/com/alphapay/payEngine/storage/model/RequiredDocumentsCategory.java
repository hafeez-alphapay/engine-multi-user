package com.alphapay.payEngine.storage.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "required_documents_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequiredDocumentsCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "doc_name", length = 250)
    private String docName;

    @Column(name = "allowed_type", length = 250)
    private String allowedType;

    @Column(name = "allowed_size", length = 20)
    private String allowedSize;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "external_file_type_id")
    private Long externalFileTypeId;

}