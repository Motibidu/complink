package com.pcgear.complink.pcgear.User.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.User.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByEmail(String email);

	Optional<UserEntity> findByUsernameAndEmail(String username, String email);

	Optional<UserEntity> findByUsername(String username);

	Page<UserEntity> findByUsernameContaining(String search, Pageable pageable);

	// Page<UserEntity> findAllByIsApprovedFalse(Pageable pageable);

}
