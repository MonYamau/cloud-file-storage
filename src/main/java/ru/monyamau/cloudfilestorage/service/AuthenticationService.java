package ru.monyamau.cloudfilestorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;
import ru.monyamau.cloudfilestorage.entity.User;
import ru.monyamau.cloudfilestorage.exception.AuthenticationException;
import ru.monyamau.cloudfilestorage.exception.UserAlreadyExistsException;
import ru.monyamau.cloudfilestorage.repository.SessionStorage;
import ru.monyamau.cloudfilestorage.repository.UserRepository;
import ru.monyamau.cloudfilestorage.util.PassHashUtil;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final SessionStorage sessionStorage;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public AuthenticationService(UserRepository userRepository, SessionStorage sessionStorage, TransactionTemplate transactionTemplate) {
        this.userRepository = userRepository;
        this.sessionStorage = sessionStorage;
        this.transactionTemplate = transactionTemplate;
    }

    public ResponseUserDto registerUser(String key, RequestUserDto userDto, int ttlMin) {
        User savedUser = transactionTemplate.execute(status -> {
            if (userRepository.existsUserByName(userDto.username())) {
                throw new UserAlreadyExistsException("Ошибка уникальности: пользователь с этим именем уже существует");
            }
            String hash = PassHashUtil.hash(userDto.password());
            return userRepository.save(new User(userDto.username(), hash));
        });
        String value = String.valueOf(savedUser.getId());
        sessionStorage.saveWithTtl(key, value, ttlMin);
        return new ResponseUserDto(savedUser.getName());
    }

    public ResponseUserDto authenticateUser(String key, RequestUserDto userDto, int ttlMin) {
        Optional<User> user = userRepository.getUserByName(userDto.username());
        if (user.isPresent()) {
            User currentUser = user.get();
            if (PassHashUtil.check(userDto.password(), currentUser.getPassword())) {
                String value = String.valueOf(currentUser.getId());
                sessionStorage.saveWithTtl(key, value, ttlMin);
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