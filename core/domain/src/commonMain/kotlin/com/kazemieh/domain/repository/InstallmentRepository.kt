package com.kazemieh.domain.repository

import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentWithRelations
import kotlinx.coroutines.flow.Flow

interface InstallmentRepository {
    fun observeInstallments(): Flow<List<InstallmentWithRelations>>
    suspend fun getInstallmentWithRelations(id: Long): InstallmentWithRelations?
    suspend fun getInstallmentById(id: Long): Installment?
    suspend fun insertInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>): Long
    suspend fun updateInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>)
    suspend fun deleteInstallment(id: Long)
}
