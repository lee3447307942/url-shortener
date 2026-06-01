package com.shorturl.service;

import com.shorturl.entity.User;

public interface UserService {
    User register(String username, String email, String password);
    User login(String email, String password);
    User getById(Long id);
    void updateDomain(Long userId, String domain);
}
