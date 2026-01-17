package dataLayer;

import java.io.Serializable;

/**
 * Represents a bill associated with a visit.
 * Holds pricing details, discounts, payment status, and payment time.
 */
public class Bill implements Serializable {
	
	/** Total amount before discounts. */
    private Double totalAmount;

    /** Discount amount applied to the bill. */
    private Double discountAmount;

    /** Final amount after discounts. */
    private Double finalAmount;

    /** Indicates whether the bill has been paid. */
    private boolean isPaid;

    /** Time when the payment was completed. */
    private DateTime paymentTime;
	
    /**
     * Returns the total amount before discounts.
     *
     * @return total amount
     */
	public double getTotalAmount() {
		return totalAmount;
	}

	/**
     * Sets the total amount before discounts.
     *
     * @param totalAmount the total amount
     */
	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}
	
	/**
     * Adds an amount to the current total.
     *
     * @param amount the amount to add
     */
	public void addToTotalAmount(double amount) {
		this.totalAmount += amount;
	}

	/**
     * Returns the discount amount.
     *
     * @return discount amount
     */
	public Double getDiscountAmount() {
		return discountAmount;
	}

	/**
     * Sets the discount amount.
     *
     * @param discountAmount the discount amount
     */
	public void setDiscountAmount(Double discountAmount) {
		this.discountAmount = discountAmount;
	}

	/**
     * Returns the final amount after discounts.
     *
     * @return final amount
     */
	public double getFinalAmount() {
		return finalAmount;
	}

	/**
     * Sets the final amount after discounts.
     *
     * @param finalAmount the final amount
     */
	public void setFinalAmount(double finalAmount) {
		this.finalAmount = finalAmount;
	}

	/**
     * Indicates whether the bill is paid.
     *
     * @return true if paid, false otherwise
     */
	public boolean isPaid() {
		return isPaid;
	}

    /**
     * Sets the payment status of the bill.
     *
     * @param isPaid payment status
     */
	public void setPaid(boolean isPaid) {
		this.isPaid = isPaid;
	}

	/**
     * Returns the payment time.
     *
     * @return payment time
     */
	public DateTime getPaymentTime() {
		return paymentTime;
	}

	/**
     * Sets the payment time.
     *
     * @param paymentTime the time of payment
     */
	public void setPaymentTime(DateTime paymentTime) {
		this.paymentTime = paymentTime;
	}
}
