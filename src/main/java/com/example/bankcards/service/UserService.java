package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // через конструктор Spring сам внедрит UserRepository
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Создание пользователя
    public User createUser(String username, String rawPassword, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User with username already exists");
        }
        String encoded = passwordEncoder.encode(rawPassword);
        User user = new User(username, encoded, role);
        return userRepository.save(user);
    }

    // Поиск по id
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Поиск по username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Получить всех пользователей
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Удаление
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
