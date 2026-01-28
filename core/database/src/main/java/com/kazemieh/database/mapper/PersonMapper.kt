package com.kazemieh.database.mapper

import com.kazemieh.common.model.Person
import com.kazemieh.database.entity.PersonEntity

fun PersonEntity.toPerson(): Person = Person(id, name, description)
fun Person.toPersonEntity(): PersonEntity =
    PersonEntity(id = id ?: 0, name = name, description = description)
