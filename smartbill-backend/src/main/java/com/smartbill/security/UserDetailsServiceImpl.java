package com.smartbill.security;

import com.smartbill.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Email/Username cannot be empty");
        }
        String cleanEmail = username.trim().toLowerCase();
        return userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + cleanEmail));
    }
}
