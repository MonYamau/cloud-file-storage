package ru.monyamau.cloudfilestorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.monyamau.cloudfilestorage.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
