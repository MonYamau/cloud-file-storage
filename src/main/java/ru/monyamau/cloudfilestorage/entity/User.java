package ru.monyamau.cloudfilestorage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "users")
@Check(constraints = "char_length(NAME) > 5 AND char_length(PASSWORD) = 60")
public class User {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "NAME", length = 40, unique = true, nullable = false)
    private String name;

    @Column(name = "PASSWORD", length = 60, nullable = false)
    private String password;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
