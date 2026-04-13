package com.cpt202.task.service;

import com.cpt202.task.entity.User;
import com.cpt202.task.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    public User updateProfile(Long userId, String bio) {
        User user = getUserById(userId);
        user.setBio(bio);
        return userRepository.save(user);
    }
}
