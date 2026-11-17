package com.kazemieh.notes.ui.edit

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.Note
import com.kazemieh.common.model.Tag
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.repository.NoteRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.*
import java.lang.reflect.Proxy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class NoteNotificationTargetTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = Notes()
    private val scheduled = mutableListOf<Pair<String, Long?>>()
    private lateinit var vm: NoteEditViewModel

    @BeforeTest fun setUp() {
        Dispatchers.setMain(dispatcher)
        val transactions = Proxy.newProxyInstance(TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)) { _, method, _ ->
            check(method.name == "observeTags")
            flowOf(emptyList<Tag>())
        } as TransactionRepository
        val scheduler = object : NotificationScheduler {
            override fun cancelReminder(id: String) = Unit
            override fun scheduleReminder(id: String, title: String, message: String,
                scheduledTime: LocalDateTime, channelId: String, extraId: Long?) {
                scheduled += id to extraId
            }
        }
        vm = NoteEditViewModel(NoOpAnalyticsService(), GetNoteByIdUseCase(repository),
            AddNoteUseCase(repository), UpdateNoteUseCase(repository), ObserveTagsUseCase(transactions),
            scheduler, ToggleNotePinUseCase(repository), ToggleNoteLockUseCase(repository))
    }

    @AfterTest fun tearDown() { vm.viewModelScope.cancel(); Dispatchers.resetMain() }

    @Test fun missingTargetCannotFallBackToCreatingANewNote() = runTest(dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effect.collect() }
        vm.onIntent(NoteEditIntent.LoadNote(42))
        runCurrent()
        vm.onIntent(NoteEditIntent.OnTitleChanged("Must not create"))
        vm.onIntent(NoteEditIntent.OnSave)
        runCurrent()
        assertEquals(42L, vm.state.value.id)
        assertEquals(0, repository.inserts)
        assertEquals(0, repository.updates)
    }

    @Test fun loadingAnotherTargetImmediatelyClearsThePreviousRecord() = runTest(dispatcher) {
        vm.onIntent(NoteEditIntent.LoadNote(1)); runCurrent()
        repository.blocked = 2
        vm.onIntent(NoteEditIntent.LoadNote(2))
        assertEquals(2L, vm.state.value.id)
        assertEquals("", vm.state.value.title)
        assertTrue(vm.state.value.isLoading)
        vm.onIntent(NoteEditIntent.OnSave); runCurrent()
        assertEquals(0, repository.updates)
    }

    @Test fun lateResponseCannotReplaceTheMostRecentTarget() = runTest(dispatcher) {
        repository.blocked = 1
        vm.onIntent(NoteEditIntent.LoadNote(1)); runCurrent()
        vm.onIntent(NoteEditIntent.LoadNote(2)); runCurrent()
        repository.release.complete(Unit); runCurrent()
        assertEquals(2L, vm.state.value.id)
        assertEquals("Note 2", vm.state.value.title)
    }

    @Test fun reminderCarriesTheFullSavedRecordId() = runTest(dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effect.collect() }
        vm.onIntent(NoteEditIntent.LoadNote(4_294_967_338)); runCurrent()
        vm.onIntent(NoteEditIntent.OnReminderChanged(1_900_000_000_000))
        vm.onIntent(NoteEditIntent.OnSave); runCurrent()
        assertEquals(listOf<Pair<String, Long?>>("note_4294967338" to 4_294_967_338L), scheduled)
        assertEquals(1, repository.updates)
    }

    @Test fun navigationBeforeSaveStartsCannotChangeTheRecordBeingSaved() = runTest(dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effect.collect() }
        vm.onIntent(NoteEditIntent.LoadNote(1)); runCurrent()
        vm.onIntent(NoteEditIntent.OnSave)
        vm.onIntent(NoteEditIntent.LoadNote(2)); runCurrent()
        assertEquals(1L, repository.savedId)
    }

    @Test fun lateSaveSuccessCannotCloseTheNextRecord() = runTest(dispatcher) {
        val effects = mutableListOf<NoteEditEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effect.collect { effects += it } }
        vm.onIntent(NoteEditIntent.LoadNote(1)); runCurrent()
        repository.holdSave = true
        vm.onIntent(NoteEditIntent.OnSave); runCurrent()
        vm.onIntent(NoteEditIntent.LoadNote(2)); runCurrent()
        repository.finishSave.complete(Unit); runCurrent()
        assertEquals(2L, vm.state.value.id)
        assertTrue(effects.isEmpty())
    }

    @Test fun aCollectorReturningAfterNavigationDoesNotReceiveOldSaveSuccess() = runTest(dispatcher) {
        vm.onIntent(NoteEditIntent.LoadNote(1)); runCurrent()
        vm.onIntent(NoteEditIntent.OnSave); runCurrent()
        vm.onIntent(NoteEditIntent.LoadNote(2)); runCurrent()
        val effects = mutableListOf<NoteEditEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effect.collect { effects += it } }
        runCurrent()
        assertTrue(effects.isEmpty())
    }

    private class Notes : NoteRepository {
        var inserts = 0
        var updates = 0
        var blocked: Long? = null
        var savedId: Long? = null
        var holdSave = false
        val finishSave = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        override fun observeNotes() = flowOf(emptyList<Note>())
        override suspend fun getNoteById(id: Long): Note? {
            if (id == blocked) release.await()
            return if (id == 42L) null else Note(id = id, title = "Note $id", content = "Body $id")
        }
        override suspend fun addNote(note: Note): Long { inserts++; return 123 }
        override suspend fun updateNote(note: Note) {
            savedId = note.id
            if (holdSave) finishSave.await()
            updates++
        }
        override suspend fun deleteNote(id: Long) = Unit
        override suspend fun togglePin(id: Long, isPinned: Boolean) = Unit
        override suspend fun toggleLock(id: Long, isLocked: Boolean) = Unit
    }
}
