package com.alphapay.payEngine.account.management.exception;


import com.alphapay.payEngine.common.exception.BaseWebApplicationException;

public class RecordNotFoundException extends BaseWebApplicationException {

	private static final long serialVersionUID = 1894679042139945335L;
	public static final String reason = "Requested Record Not Found";
	
	public RecordNotFoundException() {
		super(409, "1200", "ex.1200.entity.notfound", "Entity Not Found", "Requested Record Not Found");
	}
	
	public RecordNotFoundException(String messageKey){
		super(404, "1200", messageKey, messageKey, null);
	}
}
