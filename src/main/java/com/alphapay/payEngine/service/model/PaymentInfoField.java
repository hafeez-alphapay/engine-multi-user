package com.alphapay.payEngine.service.model;


import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "optimus_service_payment_info")
public class PaymentInfoField extends CommonBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5555859991463881726L;

	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private boolean mandatory;
	@Column(nullable = false)
	private String regex;
	@Column(name = "display_name_ar")
	private String displayNameAr;
	@Column(name = "display_name_en", nullable = false)
	private String displayNameEn;
	@Column(name = "transaction_label", nullable = false)
	private String transactionLabel;
	@Column(name = "order_no", nullable = false)
	private int orderNo;
	//This field is displayed in Payment Info Confirmation field
	@Column(nullable = false)
	private boolean displayable;
	//This field is sent to EBS
	@Column(nullable = false)
	private boolean transactionable;
	
	public PaymentInfoField() {
		super();
	}
	
	
	
	
	public boolean isTransactionable() {
		return transactionable;
	}




	public void setTransactionable(boolean transactionable) {
		this.transactionable = transactionable;
	}




	public boolean isDisplayable() {
		return displayable;
	}



	public void setDisplayable(boolean displayable) {
		this.displayable = displayable;
	}



	public int getOrderNo() {
		return orderNo;
	}



	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}



	public String getTransactionLabel() {
		return transactionLabel;
	}


	public void setTransactionLabel(String transactionLabel) {
		this.transactionLabel = transactionLabel;
	}


	public String getDisplayNameAr() {
		return displayNameAr;
	}



	public void setDisplayNameAr(String displayNameAr) {
		this.displayNameAr = displayNameAr;
	}



	public String getDisplayNameEn() {
		return displayNameEn;
	}



	public void setDisplayNameEn(String displayNameEn) {
		this.displayNameEn = displayNameEn;
	}



	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	@Override
	public String toString() {
		return "PaymentInfoField [name=" + name + ", mandatory=" + mandatory
				+ ", regex=" + regex + ", displayNameAr=" + displayNameAr
				+ ", displayNameEn=" + displayNameEn + ", transactionLabel="
				+ transactionLabel + ", orderNo=" + orderNo + ", displayable="
				+ displayable + ", transactionable=" + transactionable + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((displayNameAr == null) ? 0 : displayNameAr.hashCode());
		result = prime * result
				+ ((displayNameEn == null) ? 0 : displayNameEn.hashCode());
		result = prime * result + (displayable ? 1231 : 1237);
		result = prime * result + (mandatory ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + orderNo;
		result = prime * result + ((regex == null) ? 0 : regex.hashCode());
		result = prime
				* result
				+ ((transactionLabel == null) ? 0 : transactionLabel.hashCode());
		result = prime * result + (transactionable ? 1231 : 1237);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PaymentInfoField other = (PaymentInfoField) obj;
		if (displayNameAr == null) {
			if (other.displayNameAr != null)
				return false;
		} else if (!displayNameAr.equals(other.displayNameAr))
			return false;
		if (displayNameEn == null) {
			if (other.displayNameEn != null)
				return false;
		} else if (!displayNameEn.equals(other.displayNameEn))
			return false;
		if (displayable != other.displayable)
			return false;
		if (mandatory != other.mandatory)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (orderNo != other.orderNo)
			return false;
		if (regex == null) {
			if (other.regex != null)
				return false;
		} else if (!regex.equals(other.regex))
			return false;
		if (transactionLabel == null) {
			if (other.transactionLabel != null)
				return false;
		} else if (!transactionLabel.equals(other.transactionLabel))
			return false;
		if (transactionable != other.transactionable)
			return false;
		return true;
	}	
}
