package com.pcgear.complink.pcgear.Item;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Integer> {
        void deleteAllByItemIdIn(List<Integer> ids);

        Page<Item> findByItemNameContaining(String search, Pageable pageable);
}
