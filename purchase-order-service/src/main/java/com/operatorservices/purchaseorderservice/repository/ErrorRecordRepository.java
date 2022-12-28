package com.operatorservices.purchaseorderservice.repository;


import com.operatorservices.purchaseorderservice.model.ErrorRecord;
import org.springframework.data.repository.CrudRepository;

public interface ErrorRecordRepository extends CrudRepository<ErrorRecord, String> {
}
