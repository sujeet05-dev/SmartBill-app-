package com.smartbill.mapper;

import com.smartbill.dto.ShopDto;
import com.smartbill.entity.Shop;
import org.springframework.stereotype.Component;

@Component
public class ShopMapper {

    public ShopDto toDto(Shop shop) {
        if (shop == null) return null;
        ShopDto dto = new ShopDto();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setOwnerName(shop.getOwnerName());
        dto.setAddress(shop.getAddress());
        dto.setPhone(shop.getPhone());
        dto.setEmail(shop.getEmail());
        dto.setGstin(shop.getGstin());
        dto.setLogoUrl(shop.getLogoUrl());
        return dto;
    }

    public Shop toEntity(ShopDto dto) {
        if (dto == null) return null;
        Shop shop = new Shop();
        if (dto.getId() != null && dto.getId() != 0) {
            shop.setId(dto.getId());
        }
        shop.setName(dto.getName());
        shop.setOwnerName(dto.getOwnerName());
        shop.setAddress(dto.getAddress());
        shop.setPhone(dto.getPhone());
        shop.setEmail(dto.getEmail());
        shop.setGstin(dto.getGstin());
        shop.setLogoUrl(dto.getLogoUrl());
        return shop;
    }
}
