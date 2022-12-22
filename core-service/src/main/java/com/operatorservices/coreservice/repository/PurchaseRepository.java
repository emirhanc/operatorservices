package com.operatorservices.coreservice.repository;

import com.operatorservices.coreservice.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, String> {
}
