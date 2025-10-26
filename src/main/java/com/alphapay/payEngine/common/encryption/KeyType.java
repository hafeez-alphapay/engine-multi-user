package com.alphapay.payEngine.common.encryption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum KeyType {

	ENCRYPTION("Encryption");

	private String key;

	public void setKey(String key) {
		this.key = key;
	}
}
