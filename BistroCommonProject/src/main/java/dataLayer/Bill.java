package dataLayer;

import java.io.Serializable;

public class Bill implements Serializable {
	private double totalAmount;
	private Integer discountAmount;
	private double finalAmount;
	private boolean isPaid;
	private Visit visit;
	
	public Bill(Visit visit) {
		this.visit = visit;
		this.isPaid = false;
		this.totalAmount = 0.0;
		this.discountAmount = 0;
	}
	
	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}
	
	public void addToTotalAmount(double amount) {
		this.totalAmount += amount;
	}

	public Integer getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(Integer discountAmount) {
		this.discountAmount = discountAmount;
	}

	public double getFinalAmount() {
		return calculateFinalAmount();
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

	public Visit getVisit() {
		return visit;
	}

	public void setVisit(Visit visit) {
		this.visit = visit;
	}

	protected double calculateFinalAmount() {
		this.finalAmount = this.totalAmount * this.discountAmount;
		return this.finalAmount;
	}
	
	
}
