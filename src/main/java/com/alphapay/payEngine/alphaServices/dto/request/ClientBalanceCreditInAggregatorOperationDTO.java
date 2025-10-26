package com.alphapay.payEngine.alphaServices.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single balance-affecting operation for a client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientBalanceCreditInAggregatorOperationDTO {

    @NotNull
    @JsonProperty("clientId")
    private Long clientId;

    /** Always store money values in BigDecimal for precision. */
    @NotNull
    @Positive
    @JsonProperty("operationAmount")
    private BigDecimal operationAmount;

    private String currency;

    @NotBlank
    @JsonProperty("updatedBy")
    private String updatedBy="ALPHA PAY PAYMENT ENGINE -AUTOMATIC";

    @NotBlank
    @JsonProperty("reason")
    private String reason="SELF SERVICE CREDIT IN ALPHA GATEWAY";

    @NotNull
    @JsonProperty("operationType")
    private OperationType operationType=OperationType.CREDIT;

    @NotNull
    @JsonProperty("operationMethod")
    private OperationMethod operationMethod=OperationMethod.INSTANT_TRANSFER;

    @NotNull
    @JsonProperty("requestId")
    private String requestId;

    /** CREDIT, DEBIT, etc. Extend as needed. */
    public enum OperationType {
        CREDIT, DEBIT
    }

    /**
     * Allowed ways of applying the operation.
     */
    public enum OperationMethod {
        INSTANT_TRANSFER,
        OFFLINE_TRANSFER,
        BACK_OFFICE_MANUAL_DEBIT,
        BACK_OFFICE_MANUAL_REVERSAL,
        BACK_OFFICE_MANUAL_CREDIT
    }
}
