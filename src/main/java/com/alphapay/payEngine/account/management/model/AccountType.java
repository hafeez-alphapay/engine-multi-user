package com.alphapay.payEngine.account.management.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AccountType {

	PAN("Pan"), ACCOUNT("Account");

	private String type;

	public void setGender(String type) {
		this.type = type;
	}
}
