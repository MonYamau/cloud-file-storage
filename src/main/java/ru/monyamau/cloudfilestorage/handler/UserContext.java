package ru.monyamau.cloudfilestorage.handler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Setter
@Getter
@RequestScope
@Component
public class UserContext {
    private Integer userId;
}