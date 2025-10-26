package com.alphapay.payEngine.common.security.services;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface IKeyStore {

	PrivateKey getPrivateKey();

	PublicKey getPublicKey();

	String getPublicKeyString();
}
