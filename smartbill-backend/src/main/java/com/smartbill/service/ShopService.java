package com.smartbill.service;

import com.smartbill.dto.ShopDto;
import com.smartbill.entity.Shop;
import com.smartbill.mapper.ShopMapper;
import com.smartbill.repository.ShopRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;

    public ShopService(ShopRepository shopRepository, ShopMapper shopMapper) {
        this.shopRepository = shopRepository;
        this.shopMapper = shopMapper;
    }

    public ShopDto getShop() {
        List<Shop> shops = shopRepository.findAll();
        if (shops.isEmpty()) {
            return null;
        }
        return shopMapper.toDto(shops.get(0));
    }

    public ShopDto saveOrUpdateShop(ShopDto shopDto) {
        List<Shop> shops = shopRepository.findAll();
        Shop shop;
        if (shops.isEmpty()) {
            shop = shopMapper.toEntity(shopDto);
        } else {
            shop = shops.get(0);
            shop.setName(shopDto.getName());
            shop.setOwnerName(shopDto.getOwnerName());
            shop.setAddress(shopDto.getAddress());
            shop.setPhone(shopDto.getPhone());
            shop.setEmail(shopDto.getEmail());
            shop.setGstin(shopDto.getGstin());
            shop.setLogoUrl(shopDto.getLogoUrl());
        }
        Shop saved = shopRepository.save(shop);
        return shopMapper.toDto(saved);
    }
}
