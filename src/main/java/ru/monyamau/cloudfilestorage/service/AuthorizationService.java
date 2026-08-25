package ru.monyamau.cloudfilestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;
import ru.monyamau.cloudfilestorage.entity.User;
import ru.monyamau.cloudfilestorage.exception.AuthenticationException;
import ru.monyamau.cloudfilestorage.repository.RedisSessionStorage;
import ru.monyamau.cloudfilestorage.repository.UserRepository;
import ru.monyamau.cloudfilestorage.util.PassHashUtil;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorizationService {
    private final UserRepository userRepository;
    private final RedisSessionStorage sessionStorage;
    private final ResourceService resourceService;

    @Autowired
    public AuthorizationService(UserRepository userRepository, RedisSessionStorage sessionStorage, ResourceService resourceService) {
        this.userRepository = userRepository;
        this.sessionStorage = sessionStorage;
        this.resourceService = resourceService;
    }

    public ResponseUserDto registerUser(UUID uuid, RequestUserDto userDto, int ttlMin) {
        String hash = PassHashUtil.hash(userDto.password());
        User savedUser = userRepository.saveAndFlush(new User(userDto.username(), hash));
        resourceService.createPersonalDirectory(savedUser.getId());
        String key = String.valueOf(uuid);
        String value = String.valueOf(savedUser.getId());
        sessionStorage.save(key, value, ttlMin);
        return new ResponseUserDto(savedUser.getName());
    }

    public ResponseUserDto authorizeUser(UUID uuid, RequestUserDto userDto, int ttlMin) {
        Optional<User> user = userRepository.getUserByName(userDto.username());
        if (user.isPresent()) {
            User currentUser = user.get();
            if (PassHashUtil.check(userDto.password(), currentUser.getPassword())) {
                String key = String.valueOf(uuid);
                String value = String.valueOf(currentUser.getId());
                sessionStorage.save(key, value, ttlMin);
                return new ResponseUserDto(currentUser.getName());
            }
        }
        throw new AuthenticationException("Ошибка аутентификации: неверное имя пользователя или пароль");
    }

    public void logoutUser(String key) {
        sessionStorage.delete(key);
    }

    public ResponseUserDto findUser(UUID uuid) {
        String userId = sessionStorage.findBy(String.valueOf(uuid))
                .orElseThrow(() -> new AuthenticationException("Ошибка аутентификации: не удалось найти актуальную сессию"));
        int id = Integer.parseInt(userId);
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new IllegalStateException("Ошибка на стороне сервера: не удалось найти пользователя"));
        return new ResponseUserDto(user.getName());
    }
}