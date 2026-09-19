package com.assignment.promoengine.model;

import java.time.Instant;

public class Redemption {
    private String code;
    private String userId;
    private String orderId;
    private Instant redeemedAt;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public Instant getRedeemedAt() {
		return redeemedAt;
	}
	public void setRedeemedAt(Instant redeemedAt) {
		this.redeemedAt = redeemedAt;
	}
	public Redemption(String code, String userId, String orderId, Instant redeemedAt) {
		super();
		this.code = code;
		this.userId = userId;
		this.orderId = orderId;
		this.redeemedAt = redeemedAt;
	}
	public Redemption() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
}