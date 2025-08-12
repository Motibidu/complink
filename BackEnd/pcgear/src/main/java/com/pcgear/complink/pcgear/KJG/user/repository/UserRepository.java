package com.pcgear.complink.pcgear.KJG.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import java.util.List;




@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{

	Optional<UserEntity> findByEmail(String email);
	
	Optional<UserEntity> findByUsernameAndEmail(String username, String email);
	
	Optional<UserEntity> findByUsername(String username);
}
