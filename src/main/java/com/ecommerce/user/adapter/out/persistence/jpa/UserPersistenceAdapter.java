package com.ecommerce.user.adapter.out.persistence.jpa;

import com.ecommerce.user.adapter.out.persistence.jpa.entity.UserJpaEntity;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.usecase.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final UserSpringRepository userSpringRepository;

    public UserPersistenceAdapter(UserSpringRepository userSpringRepository) {
        this.userSpringRepository = userSpringRepository;
    }

    @Override
    public void save(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setName(user.getName());
        entity.setCreatedAt(LocalDateTime.now());
        userSpringRepository.save(entity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userSpringRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userSpringRepository.existsByEmail(email);
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getName());
    }
}
