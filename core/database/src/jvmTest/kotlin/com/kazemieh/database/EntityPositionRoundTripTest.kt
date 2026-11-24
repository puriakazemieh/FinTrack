package com.kazemieh.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.util.Properties
import kotlin.test.*

class EntityPositionRoundTripTest {
    @Test fun sourceReadsKeepPosition() = reads("source")
    @Test fun tagReadsKeepPosition() = reads("tag")
    @Test fun personReadsKeepPosition() = reads("person")
    @Test fun sourceRenameKeepsOrderAndMoney() = rename("source")
    @Test fun tagRenameKeepsOrderAndMoney() = rename("tag")
    @Test fun personRenameKeepsOrderAndMoney() = rename("person")
    private fun fixture(block: suspend (TransactionLocalDataSourceImpl) -> Unit) = runBlocking {
        val dir=Files.createTempDirectory("entity-order-")
        val driver=JdbcSqliteDriver("jdbc:sqlite:"+dir.resolve("qa.db"),Properties().apply {setProperty("foreign_keys","true")})
        try {
            FinTrackDatabase.Schema.synchronous().create(driver)
            block(TransactionLocalDataSourceImpl(FinTrackDatabase(driver,Asset.Adapter(EnumColumnAdapter()),Rate_cache.Adapter(EnumColumnAdapter()))))
        } finally {
            driver.close()
            for(s in listOf("","-journal","-wal","-shm"))Files.deleteIfExists(dir.resolve("qa.db$s"))
            Files.deleteIfExists(dir)
        }
    }
    private suspend fun seed(d:TransactionLocalDataSourceImpl,k:String,id:Long,pos:Int) { when(k){
        "source"->d.insertFullSource(Source(id=id,name="Record $id",position=pos,balance=1000,colorId=2,iconId=3,currencyCode="IRT"))
        "tag"->d.insertFullTag(Tag(id=id,name="Record $id",position=pos,colorId=2,iconId=3))
        "person"->d.insertFullPerson(Person(id=id,name="Record $id",position=pos))
        else->error(k)
    } }
    private suspend fun get(d:TransactionLocalDataSourceImpl,k:String,id:Long):Any=when(k){
        "source"->d.getSourceById(id)!!;"tag"->d.getTagById(id)!!;"person"->d.getPersonById(id)!!;else->error(k)
    }
    private suspend fun all(d:TransactionLocalDataSourceImpl,k:String):List<Any> =when(k){
        "source"->d.getAllSources();"tag"->d.getAllTags();"person"->d.getAllPersons();else->error(k)
    }
    private fun position(v:Any):Int=when(v){is Source->v.position;is Tag->v.position;is Person->v.position;else->error("Unexpected entity")}
    private fun id(v:Any):Long=when(v){is Source->v.id!!;is Tag->v.id!!;is Person->v.id!!;else->error("Unexpected entity")}
    private suspend fun move(d:TransactionLocalDataSourceImpl,k:String,id:Long,pos:Int){when(k){
        "source"->d.updateSourcePosition(id,pos);"tag"->d.updateTagPosition(id,pos);"person"->d.updatePersonPosition(id,pos);else->error(k)
    }}
    private fun reads(k:String)=fixture {d->
        seed(d,k,1,7)
        assertEquals(7,position(get(d,k,1)))
        assertEquals(7,position(all(d,k).single()))
    }
    private fun rename(k:String)=fixture {d->
        for(id in 1L..3L)seed(d,k,id,(id-1).toInt())
        if(k!="source")d.insertFullSource(Source(id=1,name="Financial",balance=1000,colorId=1,iconId=1))
        d.insertFullCategory(Category(id=1,name="Expense",type=TransactionType.EXPENSE,colorId=1,iconId=1))
        d.addTransactionWithBalance(Transaction(id=0,amount=100,categoryId=1,sourceId=1,type=TransactionType.EXPENSE,currencyCode="IRT"),
            if(k=="tag")listOf(1L) else emptyList(),if(k=="person")listOf(1L) else emptyList(),mapOf(1L to -100L))
        move(d,k,1,2);move(d,k,2,0);move(d,k,3,1)
        val before=all(d,k).map(::id)
        assertEquals(listOf(2L,3L,1L),before)
        when(val v=get(d,k,1)){
            is Source->d.updateSource(v.copy(name="Renamed"))
            is Tag->d.updateTag(v.copy(name="Renamed"))
            is Person->d.updatePerson(v.copy(name="Renamed"))
        }
        assertEquals(before,all(d,k).map(::id))
        assertEquals(2,position(get(d,k,1)))
        assertEquals(900L,d.getSourceById(1)!!.balance)
        assertEquals(1L,d.getTransactionCount())
    }
}
