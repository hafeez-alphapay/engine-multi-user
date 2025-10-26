package com.alphapay.payEngine.account.management.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Pagination {
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private long totalRecords;

    public Pagination(int currentPage, int totalPages, int pageSize, long totalRecords) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalRecords = totalRecords;
    }

    // Getters and setters
}
