package com.alphapay.payEngine.account.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PaginatedMerchantResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean lastPage;
    private long countActiveUsersLast30Days ;
    private long countApprovedMbme ;
    private long countApprovedMyfattora;
}