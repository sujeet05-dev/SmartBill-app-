package com.smartbill.service;

import com.smartbill.dto.ShopDto;
import com.smartbill.entity.Shop;
import com.smartbill.entity.User;
import com.smartbill.mapper.ShopMapper;
import com.smartbill.repository.ShopRepository;
import com.smartbill.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    private final SecurityUtils securityUtils;

    public ShopService(ShopRepository shopRepository, ShopMapper shopMapper, SecurityUtils securityUtils) {
        this.shopRepository = shopRepository;
        this.shopMapper = shopMapper;
        this.securityUtils = securityUtils;
    }

    public ShopDto getShop() {
        User currentUser = securityUtils.getCurrentUser();
        Optional<Shop> shopOpt = shopRepository.findByUser(currentUser);
        return shopOpt.map(shopMapper::toDto).orElse(null);
    }

    public ShopDto saveOrUpdateShop(ShopDto shopDto) {
        User currentUser = securityUtils.getCurrentUser();
        Optional<Shop> shopOpt = shopRepository.findByUser(currentUser);
        
        Shop shop;
        if (shopOpt.isEmpty()) {
            shop = shopMapper.toEntity(shopDto);
            shop.setUser(currentUser);
        } else {
            shop = shopOpt.get();
            shop.setName(shopDto.getName());
            shop.setOwnerName(shopDto.getOwnerName());
            shop.setAddress(shopDto.getAddress());
            shop.setState(shopDto.getState());
            shop.setPincode(shopDto.getPincode());
            shop.setPhone(shopDto.getPhone());
            shop.setEmail(shopDto.getEmail());
            shop.setGstin(shopDto.getGstin());
            shop.setLogoUrl(shopDto.getLogoUrl());
            shop.setTermsAndConditions(shopDto.getTermsAndConditions());
        }
        
        Shop saved = shopRepository.save(shop);
        return shopMapper.toDto(saved);
    }
}
