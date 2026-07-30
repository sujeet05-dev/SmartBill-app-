package com.smartbill.controller;

import com.smartbill.dto.ShopDto;
import com.smartbill.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shop")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping
    public ResponseEntity<ShopDto> getShop() {
        ShopDto shop = shopService.getShop();
        if (shop == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(shop);
    }

    @PutMapping
    public ResponseEntity<ShopDto> saveOrUpdateShop(@Valid @RequestBody ShopDto shopDto) {
        return ResponseEntity.ok(shopService.saveOrUpdateShop(shopDto));
    }
}
