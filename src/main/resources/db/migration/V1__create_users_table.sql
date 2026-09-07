create table users
(
    ID       int auto_increment primary key,
    NAME     varchar(40) not null,
    PASSWORD varchar(60) not null,
    constraint NAME unique (NAME),
    constraint user_name_min_length
        check (char_length(`NAME`) > 5),
    constraint user_password_length
        check (char_length(`PASSWORD`) = 60)
);
