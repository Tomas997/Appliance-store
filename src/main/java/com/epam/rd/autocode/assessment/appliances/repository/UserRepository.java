package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
