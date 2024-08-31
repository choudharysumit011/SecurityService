package com.tutor.securityservice.repo;


import com.tutor.securityservice.dto.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepo extends MongoRepository<User, String> {
        Optional<User> findByEmail(String email);

        Optional<User> findByUsername(String username);
}
