package com.assignment.promoengine.repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Repository;

import com.assignment.promoengine.model.PromoCode;
import com.assignment.promoengine.model.Redemption;

@Repository
public class PromoCodeRepository {
	private final ConcurrentHashMap<String, PromoCode> promoCodes = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, java.util.Set<String>> userRedemptions = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
	// Using ConcurrentHashMap to safely store codes in memory
	private final ConcurrentHashMap<String, PromoCode> storage = new ConcurrentHashMap<>();

	public ReentrantLock getLock(String code) {
		return locks.computeIfAbsent(code, k -> new ReentrantLock(true));
	}

	public PromoCode save(PromoCode promoCode) {
		promoCodes.put(promoCode.getCode(), promoCode);
		userRedemptions.putIfAbsent(promoCode.getCode(), ConcurrentHashMap.newKeySet());
		return promoCode;
	}

	public PromoCode findByCode(String code) {
		return promoCodes.get(code);
	}

	public boolean hasUserRedeemed(String code, String userId) {
		java.util.Set<String> redeemedUsers = userRedemptions.get(code);
		return redeemedUsers != null && redeemedUsers.contains(userId);
	}

	public void addRedemption(Redemption redemption) {
		userRedemptions.computeIfAbsent(redemption.getCode(), k -> ConcurrentHashMap.newKeySet())
				.add(redemption.getUserId());
		PromoCode promo = promoCodes.get(redemption.getCode());
		if (promo != null) {
			promo.setCurrentRedemptions(promo.getCurrentRedemptions() + 1);
		}
	}

	public void clear() {
		storage.clear();
	}
}