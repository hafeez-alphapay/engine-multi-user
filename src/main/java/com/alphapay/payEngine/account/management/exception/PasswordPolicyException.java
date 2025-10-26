package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;
import com.alphapay.payEngine.management.data.FinancialInstitutionPasswordPolicy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordPolicyException extends BaseWebApplicationException {
    private FinancialInstitutionPasswordPolicy policy;
    public PasswordPolicyException(FinancialInstitutionPasswordPolicy policy) {
        super(401, "7208", "ex.7208.password.policy.notmatch", "Password is not matching policy", "Password is not matching policy");
        this.policy=policy;
    }
    public PasswordPolicyException() {
        super(401, "7208", "ex.7208.password.policy.notmatch", "Password is not matching policy", "Password is not matching policy");
    }


}
