package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.*

class CategoryPositionRoundTripTest {
    private fun fixture(block: suspend (TransactionLocalDataSourceImpl) -> Unit) = runBlocking {
        val dir=Files.createTempDirectory("category-position-")
        val driver=JdbcSqliteDriver("jdbc:sqlite:"+dir.resolve("qa.db"))
        try {
            FinTrackDatabase.Schema.synchronous().create(driver)
            val db=FinTrackDatabase(driver,Asset.Adapter(EnumColumnAdapter()),Rate_cache.Adapter(EnumColumnAdapter()))
            block(TransactionLocalDataSourceImpl(db))
        } finally {
            driver.close()
            for(s in listOf("","-journal","-wal","-shm")) Files.deleteIfExists(dir.resolve("qa.db$s"))
            Files.deleteIfExists(dir)
        }
    }
    @Test fun persistedPositionSurvivesEveryCategoryReadPath() = fixture { data ->
        val category=Category(id=1,name="Positioned",position=7,type=TransactionType.EXPENSE,colorId=2,iconId=3)
        data.insertFullCategory(category)
        assertEquals(category,data.getCategoryById(1))
        assertEquals(7,data.getAllCategories().single().position)
        assertEquals(7,data.observeCategories(TransactionType.EXPENSE,null).first().single().position)
    }
    @Test fun renameAfterReorderKeepsOrderAndFinancialData() = fixture { data ->
        data.insertFullSource(Source(id=1,name="QA",balance=1000,colorId=1,iconId=1))
        for(id in 1L..3L)data.insertFullCategory(Category(id=id,name="Category $id",type=TransactionType.EXPENSE,
            position=(id-1).toInt(),colorId=1,iconId=1))
        data.addTransactionWithBalance(Transaction(id=0,amount=100,categoryId=1,sourceId=1,currencyCode="IRT",
            type=TransactionType.EXPENSE),emptyList(),emptyList(),mapOf(1L to -100L))
        data.updateCategoryPosition(1,2);data.updateCategoryPosition(2,0);data.updateCategoryPosition(3,1)
        val before=data.observeCategories(TransactionType.EXPENSE,null).first().map { it.id }
        assertEquals(listOf(2L,3L,1L),before)
        val source=data.getSourceById(1)
        val edited=data.getCategoryById(1)!!.copy(name="Renamed")
        data.updateCategory(edited)
        assertEquals(before,data.observeCategories(TransactionType.EXPENSE,null).first().map { it.id })
        assertEquals(2,data.getCategoryById(1)!!.position)
        assertEquals("Renamed",data.getCategoryById(1)!!.name)
        assertEquals(source,data.getSourceById(1))
        assertEquals(1L,data.getTransactionCount())
    }
}
