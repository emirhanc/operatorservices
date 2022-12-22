package com.operatorservices.coreservice.repository;

import com.operatorservices.coreservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}
