package com.alphapay.payEngine.account.management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDetails {

	@Column(name = "full_name", nullable = false, length = 255)
	private String fullName;

	@Column(name = "country_code", length = 15)
	private String countryCode;

	@Column(name = "mobile_no", length = 15)
	private String mobileNo;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;


}