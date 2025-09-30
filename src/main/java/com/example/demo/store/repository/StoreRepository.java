package com.example.demo.store.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.store.entity.Store;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
}