package com.yaren.careerpilot.repository;

import com.yaren.careerpilot.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = new User();
        user.setFullName("Yaren Keles");
        user.setEmail("yaren@test.com");
        user.setPassword("hashedpassword");
        userRepository.save(user);
    }
    @Test
    void findByEmail_existingEmail_returnsUser() {
        Optional<User> result = userRepository.findByEmail("yaren@test.com");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("yaren@test.com");
        assertThat(result.get().getFullName()).isEqualTo("Yaren Keles");
    }
    @Test
    void findByEmail_nonExistingEmail_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("nobody@test.com");
        assertThat(result).isEmpty();
    }
    @Test
    void existsByEmail_existingEmail_returnsTrue() {
        boolean exists = userRepository.existsByEmail("yaren@test.com");
        assertThat(exists).isTrue();
    }
    @Test
    void existsByEmail_nonExistingEmail_returnsFalse() {
        boolean exists = userRepository.existsByEmail("nobody@test.com");
        assertThat(exists).isFalse();
    }
}