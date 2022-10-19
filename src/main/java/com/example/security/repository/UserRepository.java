package com.example.security.repository;

import com.example.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

// CURD 함수를 JpaRepository 가 들고 있음.
// @Repository 라는 어노테이션이 없어도 IoC됨. JpaRepository 를 상속했기 때문에
public interface UserRepository extends JpaRepository<User, Integer> {
    // findBy 규치 -> Username 문법
    // select * from user where username = 1?
    User findByUsername(String username);


}
