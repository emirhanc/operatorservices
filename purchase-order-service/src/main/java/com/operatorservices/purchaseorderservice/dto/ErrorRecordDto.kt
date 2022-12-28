package com.operatorservices.purchaseorderservice.dto

import com.operatorservices.purchaseorderservice.model.ErrorRecord

data class ErrorRecordDto (
    val id: String?,
    val code: Short,
    val message: String
){
    companion object{
        @JvmStatic  //for Java Interoperability, static getter/setter methods
        fun convertToDto(errorRecord: ErrorRecord): ErrorRecordDto {
            return ErrorRecordDto(
                errorRecord.id,
                errorRecord.code,
                errorRecord.message
            )
        }
    }
}
