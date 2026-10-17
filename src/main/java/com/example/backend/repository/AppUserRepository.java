package com.example.backend.repository;

import com.example.backend.model.AppUser;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);

	Optional<AppUser> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select user from AppUser user where lower(user.username) = lower(:username)")
	Optional<AppUser> findByUsernameForUpdate(@Param("username") String username);
}
