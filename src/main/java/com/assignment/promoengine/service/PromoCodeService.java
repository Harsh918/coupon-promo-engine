package com.assignment.promoengine.service;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.assignment.promoengine.model.PromoCode;
import com.assignment.promoengine.model.Redemption;
import com.assignment.promoengine.repository.PromoCodeRepository;

@Service
public class PromoCodeService {

    @Autowired
    private PromoCodeRepository repository;

    public PromoCode createPromoCode(PromoCode promoCode) {
        promoCode.setCurrentRedemptions(0);
        promoCode.setActive(true);
        return repository.save(promoCode);
    }

    public PromoCode getPromoCode(String code) {
        return repository.findByCode(code);
    }

    public synchronized Redemption redeemCode(String code, String userId, String orderId) {
        ReentrantLock lock = repository.getLock(code);
        lock.lock();
        try {
            PromoCode promo = repository.findByCode(code);
            if (promo == null || !promo.isActive()) {
                throw new IllegalArgumentException("Promo code is invalid or inactive");
            }

            if (repository.hasUserRedeemed(code, userId)) {
                throw new IllegalStateException("User has already redeemed this promo code");
            }

            if (promo.getCurrentRedemptions() >= promo.getMaxRedemptions()) {
                throw new IllegalStateException("Promo code redemption limit reached");
            }

            Redemption redemption = new Redemption(code, userId, orderId, Instant.now());
            repository.addRedemption(redemption);
            return redemption;
        } finally {
            lock.unlock();
        }
    }
}