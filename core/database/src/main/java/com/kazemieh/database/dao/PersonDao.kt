package com.kazemieh.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kazemieh.database.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Insert
    suspend fun addPerson(person: PersonEntity): Long

    @Insert
    suspend fun insertAllPerson(person: List<PersonEntity>): List<Long>

    @Query("SELECT * FROM person")
    fun observePersons(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM person WHERE id = :personId")
    suspend fun getPersonById(personId: Long): PersonEntity

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    @Update
    suspend fun updatePerson(person: PersonEntity): Int
}

