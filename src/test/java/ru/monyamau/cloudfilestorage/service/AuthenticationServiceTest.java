package ru.monyamau.cloudfilestorage.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.monyamau.cloudfilestorage.BaseContextTest;
import ru.monyamau.cloudfilestorage.dto.request.RequestUserDto;
import ru.monyamau.cloudfilestorage.dto.response.ResponseUserDto;
import ru.monyamau.cloudfilestorage.exception.AuthenticationException;
import ru.monyamau.cloudfilestorage.exception.UserAlreadyExistsException;
import ru.monyamau.cloudfilestorage.repository.SessionStorage;
import ru.monyamau.cloudfilestorage.repository.UserRepository;

import java.util.UUID;

@Transactional
public class AuthenticationServiceTest extends BaseContextTest {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionStorage sessionStorage;

    @Test
    @DisplayName("При регистрации пользователя должны создаться записи в MySQL и Redis")
    void shouldRegisterUser() {
        String key = String.valueOf(UUID.randomUUID());
        RequestUserDto requestUserDto = new RequestUserDto("username", "password");
        int ttlMin = 1;
        ResponseUserDto responseUserDto = authenticationService.registerUser(key, requestUserDto, ttlMin);
        assert userRepository.existsUserByName(responseUserDto.username());
        assert sessionStorage.findBy(key).isPresent();
    }

    @Test
    @DisplayName("При повторной регистрации пользователя ожидается исключение UserAlreadyExistsException")
    void shouldNotRegisterExistingUser() {
        String key = String.valueOf(UUID.randomUUID());
        RequestUserDto requestUserDto = new RequestUserDto("username", "password");
        int ttlMin = 1;
        authenticationService.registerUser(key, requestUserDto, ttlMin);
        Assertions.assertThrows(UserAlreadyExistsException.class,
                () -> authenticationService.registerUser(key, requestUserDto, ttlMin));
    }

    @Test
    @DisplayName("Пользователь должен успешно выйти и авторизоваться")
    void shouldLogoutAndAuthenticateUser() {
        String key = String.valueOf(UUID.randomUUID());
        RequestUserDto requestUserDto = new RequestUserDto("username", "password");
        int ttlMin = 1;
        authenticationService.registerUser(key, requestUserDto, ttlMin);
        authenticationService.logoutUser(key);
        assert sessionStorage.findBy(key).isEmpty();
        authenticationService.authenticateUser(key, requestUserDto, ttlMin);
        assert sessionStorage.findBy(key).isPresent();
    }

    @Test
    @DisplayName("Пользователь не должен авторизоваться с некорректным паролем")
    void shouldNotAuthenticateUserWithIncorrectPassword() {
        String key = String.valueOf(UUID.randomUUID());
        RequestUserDto requestUserDto = new RequestUserDto("username", "password");
        int ttlMin = 1;
        RequestUserDto requestUserDtoWithIncorrectPassword = new RequestUserDto("username", "password!");
        authenticationService.registerUser(key, requestUserDto, ttlMin);
        authenticationService.logoutUser(key);
        assert sessionStorage.findBy(key).isEmpty();
        Assertions.assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticateUser(key, requestUserDtoWithIncorrectPassword, ttlMin));
    }

    @Test
    @DisplayName("Пользователь должен найтись по ключу сессии")
    void shouldFindAuthenticatedUser() {
        String key = String.valueOf(UUID.randomUUID());
        RequestUserDto requestUserDto = new RequestUserDto("username", "password");
        int ttlMin = 1;
        authenticationService.registerUser(key, requestUserDto, ttlMin);
        ResponseUserDto userDto = authenticationService.findUser(key);
        assert userDto.username().equals(requestUserDto.username());
    }
}
