package ru.monyamau.cloudfilestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.entity.User;
import ru.monyamau.cloudfilestorage.repository.RedisSessionStorage;
import ru.monyamau.cloudfilestorage.repository.UserRepository;
import ru.monyamau.cloudfilestorage.util.PassHashUtil;

import java.util.UUID;

@Service
public class AuthorizationService {
    private final UserRepository userRepository;
    private final RedisSessionStorage sessionStorage;

    @Autowired
    public AuthorizationService(UserRepository userRepository, RedisSessionStorage sessionStorage) {
        this.userRepository = userRepository;
        this.sessionStorage = sessionStorage;
    }

    public String registerUser(UUID uuid, RequestUserDto userDto, int ttlMin) {
        String hash = PassHashUtil.hash(userDto.password());
        User savedUser = userRepository.saveAndFlush(new User(userDto.username(), hash));
        sessionStorage.save(String.valueOf(uuid), String.valueOf(savedUser.getId()), ttlMin);
        return savedUser.getName();
    }

    public String authorizeUser(UUID uuid, RequestUserDto userDto, int ttlMin) {
        User user = userRepository.getUserByName(userDto.username()).orElseThrow(() -> new RuntimeException("UncorrectedName"));
        if (!user.getName().equals(userDto.username()) && !PassHashUtil.check(userDto.password(), user.getPassword())) {
            throw new RuntimeException("UncorrectedPassword");
        }
        sessionStorage.save(String.valueOf(uuid), String.valueOf(user.getId()), ttlMin);
        return user.getName();
    }

    public void logoutUser(String key) {
        sessionStorage.delete(key);
    }
}