package com.assignment.promoengine.controller;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assignment.promoengine.model.PromoCode;
import com.assignment.promoengine.model.Redemption;
import com.assignment.promoengine.service.PromoCodeService;

@RestController
@RequestMapping("/promo-codes")
public class PromoCodeController {

    @Autowired
    private PromoCodeService promoCodeService;

    @PostMapping
    public ResponseEntity<PromoCode> createPromoCode(@RequestBody PromoCode promoCode) {
        PromoCode created = promoCodeService.createPromoCode(promoCode);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getPromoCode(@PathVariable String code) {
        PromoCode promo = promoCodeService.getPromoCode(code);
        if (promo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Promo code not found");
        }
        int remaining = Math.max(0, promo.getMaxRedemptions() - promo.getCurrentRedemptions());
        return ResponseEntity.ok(Map.of(
            "code", promo.getCode(),
            "maxRedemptions", promo.getMaxRedemptions(),
            "currentRedemptions", promo.getCurrentRedemptions(),
            "remainingRedemptions", remaining,
            "active", promo.isActive()
        ));
    }

    @PostMapping("/{code}/redeem")
    public ResponseEntity<?> redeemCode(@PathVariable String code, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String orderId = request.get("orderId");

        if (userId == null || orderId == null) {
            return ResponseEntity.badRequest().body("userId and orderId are required");
        }

        try {
            Redemption redemption = promoCodeService.redeemCode(code, userId, orderId);
            return ResponseEntity.ok(redemption);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}