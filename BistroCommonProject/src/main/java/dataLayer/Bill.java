package dataLayer;

import java.io.Serializable;

public class Bill implements Serializable {
	private Double totalAmount;
	private Double discountAmount;
	private Double finalAmount;
	private boolean isPaid;
	private DateTime paymentTime;
	
	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}
	
	public void addToTotalAmount(double amount) {
		this.totalAmount += amount;
	}

	public Double getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(Double discountAmount) {
		this.discountAmount = discountAmount;
	}

	public double getFinalAmount() {
		return finalAmount;
	}

	public void setFinalAmount(double finalAmount) {
		this.finalAmount = finalAmount;
	}

	public boolean isPaid() {
		return isPaid;
	}

	public void setPaid(boolean isPaid) {
		this.isPaid = isPaid;
	}

	public DateTime getPaymentTime() {
		return paymentTime;
	}

	public void setPaymentTime(DateTime paymentTime) {
		this.paymentTime = paymentTime;
	}
	
	
}
