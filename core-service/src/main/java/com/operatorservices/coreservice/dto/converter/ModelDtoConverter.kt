package com.operatorservices.coreservice.dto.converter

import com.operatorservices.coreservice.model.Account
import com.operatorservices.coreservice.model.Customer
import com.operatorservices.coreservice.model.Purchase
import com.operatorservices.coreservice.model.SubPackage
import com.operatorservices.coreservice.dto.*
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface ModelDtoConverter {

    fun customerToCustomerDto(customer: Customer): CustomerDto
    fun customerToCustomerGetDto(customer: Customer): CustomerGetDto

    fun accountToAccountDto(account: Account): AccountDto
    fun accountToGetAccountsByPackageDto(account: Account): GetAccountsByPackageDto
    fun accountToCustomerRequestGetAccountDto(account: Account): CustomerRequestGetAccountDto

    fun purchaseToPurchaseDto(purchase: Purchase): PurchaseDto

    fun packageToPackageDto(subPackage: SubPackage): PackageDto
}