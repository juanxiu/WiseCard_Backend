package com.example.demo.benefit.repository;

import com.example.demo.benefit.entity.DiscountBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountBenefitRepository extends JpaRepository<DiscountBenefit, Long>{
}
