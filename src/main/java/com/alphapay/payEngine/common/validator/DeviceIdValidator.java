package com.alphapay.payEngine.common.validator;

import com.alphapay.payEngine.common.bean.AuditInfo;
import com.alphapay.payEngine.common.validator.ValidDeviceId;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceIdValidator implements
		ConstraintValidator<ValidDeviceId, AuditInfo> {
	private static final Logger log = LoggerFactory.getLogger(DeviceIdValidator.class);
	@Override
	public void initialize(ValidDeviceId arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isValid(AuditInfo value, ConstraintValidatorContext cvc) {
		
		if(value == null || value.getDeviceId()==null)
			return false;
		
		return isValidDeviceId(value.getDeviceId());
	}

	static boolean isValidDeviceId(String deviceId)
	{
		log.debug("validating deviceID {} with length {}",deviceId,deviceId.length());
		//TODO check all formats and create validation
		return deviceId.length()>4;
	}

}
