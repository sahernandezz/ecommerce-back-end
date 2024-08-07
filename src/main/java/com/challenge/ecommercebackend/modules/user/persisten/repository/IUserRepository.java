package com.challenge.ecommercebackend.modules.user.persisten.repository;

import com.challenge.ecommercebackend.modules.user.persisten.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
