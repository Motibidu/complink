package com.pcgear.complink.pcgear.Item;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ItemRepository extends JpaRepository<Item, Integer> {
        void deleteAllByItemIdIn(List<Integer> ids);

        Page<Item> findByItemNameContaining(String search, Pageable pageable);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select i from Item i where i.itemId in :itemIds")
        List<Item> findAllByItemIdInWithLock(@Param("itemIds") List<Integer> itemIds);

}
