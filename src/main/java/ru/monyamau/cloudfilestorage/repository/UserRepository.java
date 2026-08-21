package ru.monyamau.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.monyamau.cloudfilestorage.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> getUserByName(String name);

    Optional<User> findUserById(int id);
}
