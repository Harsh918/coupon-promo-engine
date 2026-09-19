package com.assignment.promoengine.model;

public class PromoCode {
    private String code;
    private int maxRedemptions;
    private int currentRedemptions;
    private boolean active;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getMaxRedemptions() {
		return maxRedemptions;
	}
	public void setMaxRedemptions(int maxRedemptions) {
		this.maxRedemptions = maxRedemptions;
	}
	public int getCurrentRedemptions() {
		return currentRedemptions;
	}
	public void setCurrentRedemptions(int currentRedemptions) {
		this.currentRedemptions = currentRedemptions;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public PromoCode(String code, int maxRedemptions, int currentRedemptions, boolean active) {
	//	super();
		this.code = code;
		this.maxRedemptions = maxRedemptions;
		this.currentRedemptions = currentRedemptions;
		this.active = active;
	}
	public PromoCode(String code, int maxRedemptions) {
		//super();
		this.code = code;
		this.maxRedemptions = maxRedemptions;
	}
	public PromoCode() {
		super();
		// TODO Auto-generated constructor stub
	}



}