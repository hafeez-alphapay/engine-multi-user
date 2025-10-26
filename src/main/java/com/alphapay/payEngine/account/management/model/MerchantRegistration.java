package com.alphapay.payEngine.account.management.model;

import com.alphapay.payEngine.common.bean.CommonBean;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;

import java.io.Serial;
import java.util.Date;

@Entity
@Setter
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(name = "registration")
@AttributeOverrides({
        @AttributeOverride(name = "mobileNo", column = @Column(unique = false, nullable = true))
})
public class MerchantRegistration extends CommonBean {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 4361094317349480940L;

    @Valid
    @JsonUnwrapped
    @Embedded
    @AttributeOverrides(
            {
                    @AttributeOverride(name = "email", column = @Column(unique = false, nullable = true))
            }
    )
    private UserDetails userDetails;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "is_mobile_verified")
    private boolean isMobileVerified;

    @Column(name = "is_email_verified")
    private boolean isEmailVerified;

    private Date activationDate;

    private String requestId;

    private String registrationId;

    private String applicationId;

    @Transient
    String message;
    @Transient
    String messageAr;
    @Transient
    String confirmPassword;


}
