package ru.monyamau.cloudfilestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;
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

    public ResponseUserDto registerUser(UUID uuid, RequestUserDto userDto, int ttlMin) {
        String hash = PassHashUtil.hash(userDto.password());
        User savedUser = userRepository.saveAndFlush(new User(userDto.username(), hash));
        sessionStorage.save(String.valueOf(uuid), String.valueOf(savedUser.getId()), ttlMin);
        return new ResponseUserDto(savedUser.getName());
    }

    public ResponseUserDto authorizeUser(UUID uuid, RequestUserDto userDto, int ttlMin) {
        User user = userRepository.getUserByName(userDto.username()).orElseThrow(() -> new RuntimeException("UncorrectedName"));
        if (user.getName().equals(userDto.username()) && PassHashUtil.check(userDto.password(), user.getPassword())) {
            sessionStorage.save(String.valueOf(uuid), String.valueOf(user.getId()), ttlMin);
            return new ResponseUserDto(user.getName());
        }
        throw new RuntimeException("UncorrectedPassword");
    }

    public void logoutUser(String key) {
        sessionStorage.delete(key);
    }

    public ResponseUserDto findUser(UUID uuid) {
        String userId = sessionStorage.findBy(String.valueOf(uuid)).orElseThrow(RuntimeException::new);
        int id = Integer.parseInt(userId);
        User user = userRepository.findUserById(id).orElseThrow(RuntimeException::new);
        return new ResponseUserDto(user.getName());
    }
}