package com.tutor.securityservice.repo;


import com.tutor.securityservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepo extends MongoRepository<User, String> {
        Optional<User> findByEmail(String email);
        Optional<User> findByVerificationCode(String verificationCode);

        Optional<User> findByUsername(String username);
}
