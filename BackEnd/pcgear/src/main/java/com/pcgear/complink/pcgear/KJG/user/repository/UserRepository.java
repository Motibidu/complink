package com.pcgear.complink.pcgear.KJG.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.KJG.user.entity.UserEntity;
import java.util.List;




@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{

	UserEntity findByEmail(String email);
	
	UserEntity findByUsername(String username);
}
