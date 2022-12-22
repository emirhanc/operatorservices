package com.operatorservices.coreservice.repository;

import com.operatorservices.coreservice.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomerRepository extends JpaRepository<Customer, String> {
}
