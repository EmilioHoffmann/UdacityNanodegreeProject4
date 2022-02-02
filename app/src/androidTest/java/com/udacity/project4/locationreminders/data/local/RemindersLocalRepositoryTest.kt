package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var repository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    private val sampleReminder = ReminderDTO(
        "Title1",
        "Description1",
        "Location1",
        11.11,
        11.11
    )

    @Test
    fun addReminder_VerifyInsertion_Success() = runBlocking {
        repository.saveReminder(sampleReminder)

        val recoveredReminder = repository.getReminder(sampleReminder.id)
        recoveredReminder as Result.Success
        assertThat(recoveredReminder.data.id, `is`(sampleReminder.id))
        assertThat(recoveredReminder.data.title, `is`(sampleReminder.title))
        assertThat(recoveredReminder.data.description, `is`(sampleReminder.description))
        assertThat(recoveredReminder.data.location, `is`(sampleReminder.location))
        assertThat(recoveredReminder.data.latitude, `is`(sampleReminder.latitude))
        assertThat(recoveredReminder.data.longitude, `is`(sampleReminder.longitude))
    }

    @Test
    fun removeReminders_AddReminderAndRemoveReminders_ShouldReturnEmptyList() = runBlocking {
        repository.saveReminder(sampleReminder)

        val recoveredList = repository.getReminders()
        recoveredList as Result.Success
        assertThat(recoveredList.data.size, `is`(1))

        repository.deleteAllReminders()

        val afterDeletionRecoveredList = repository.getReminders()
        afterDeletionRecoveredList as Result.Success
        assertThat(afterDeletionRecoveredList.data.size, `is`(0))
    }

    @Test
    fun emptyListError() = runBlocking {
        val recoveredReminder = repository.getReminder("Id")

        recoveredReminder as Result.Error
        assertThat(recoveredReminder.message, `is`("Reminder not found!"))
    }
}
