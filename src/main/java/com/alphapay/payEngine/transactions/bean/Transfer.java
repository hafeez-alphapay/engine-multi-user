package com.alphapay.payEngine.transactions.bean;

import com.alphapay.payEngine.integration.dto.paymentData.BaseFinancialRequest;
import com.alphapay.payEngine.utilities.BeanUtility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transfer extends BaseFinancialRequest {

    @NotBlank(message="toAccountIdentifier can't be null")
    private String toAccountIdentifier;
    @NotBlank
    @Pattern(regexp = "(ACCOUNT|PAN)",message = "Either ACCOUNT or PAN only are allowed")
    private String toAccountType;
    @NotNull(message = "Amount can't be null")
    @DecimalMin(value = "1", inclusive = true,message = "Amount cant be less than 1")
    BigDecimal amount;
    @JsonIgnore
    private String toAccountIdentifierEncryptedValue;
    //responseFields
    String transactionNumber;
    //EBS Responses
    String transactionId;

    String beneficiaryName;

    String RRN;
    private String toCif;

    private String fromAccountTypeId;
    private String toAccountTypeId;

    private String toAccountBranch;
    private String fromAccountBranch;
    private String toAccountCurrency;
    private String fromAccountCurrency;
    private String transferNote;



    //total=amount+fee+tax
    @DecimalMin(value = "1", inclusive = true,message = "Amount cant be less than 1")
    BigDecimal totalAmount;

    @Override
    public String toString() {
        return "Transfer{" +
                "toAccountIdentifier='" + (toAccountType!=null && toAccountType.equals("PAN")? BeanUtility.maskPan(toAccountIdentifier):toAccountIdentifier) + '\'' +
                ", toAccountType='" + toAccountType + '\'' +
                ", amount=" + amount +
                ", totalAmount=" + totalAmount +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", RRN='" + RRN + '\'' +
                '}';
    }
}
