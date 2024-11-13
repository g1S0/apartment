package org.apartment.auth.repository;

import java.util.Optional;
import org.apartment.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer>
{
  Optional<User> findByEmail(String email);
}
