package com.pcgear.complink.pcgear.PJH.Order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pcgear.complink.pcgear.PJH.Order.model.Manager;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, String> {
    void deleteAllByManagerIdIn(List<String> managerIds);
}
