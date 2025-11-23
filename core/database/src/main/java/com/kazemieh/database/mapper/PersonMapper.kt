package com.kazemieh.database.mapper

import com.kazemieh.database.entity.PersonEntity
import com.kazemieh.model.Person

fun PersonEntity.toPerson(): Person = Person(id, name, description)
fun Person.toPersonEntity(): PersonEntity = PersonEntity(name = name, description = description)
