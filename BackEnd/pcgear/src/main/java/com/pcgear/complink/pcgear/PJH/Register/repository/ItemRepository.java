package com.pcgear.complink.pcgear.PJH.Register.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pcgear.complink.pcgear.PJH.Register.model.Item;

public interface ItemRepository extends JpaRepository<Item, Integer> {
        void deleteAllByItemIdIn(List<Integer> ids);
}
