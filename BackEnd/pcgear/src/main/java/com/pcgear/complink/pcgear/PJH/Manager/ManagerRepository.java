package com.pcgear.complink.pcgear.PJH.Manager;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, String> {
    void deleteAllByManagerIdIn(List<String> managerIds);
}
