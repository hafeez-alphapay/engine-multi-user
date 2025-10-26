package com.alphapay.payEngine.alphaServices.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "product_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductInventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "stock", nullable = false)
    private Integer stock; // Current available stock

    @Column(name = "initial_stock", nullable = false)
    private Integer initialStock; // Initial stock when created

    @Column(name = "reserved_stock", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer reservedStock = 0; // Reserved stock for pending orders

    @Column(name = "low_stock_threshold", nullable = false, columnDefinition = "INT DEFAULT 5")
    private Integer lowStockThreshold = 5; // Threshold for low-stock alerts

    @Column(name = "created_on", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdOn; // Timestamp for record creation

    @Column(name = "updated_on", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedOn; // Timestamp for record updates
}
