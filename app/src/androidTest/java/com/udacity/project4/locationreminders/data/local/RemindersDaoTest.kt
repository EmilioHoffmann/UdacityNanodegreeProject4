package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
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
    fun saveReminder_RecoverReminder_RemindersAreEqual() = runBlockingTest {
        database.reminderDao().saveReminder(sampleReminder)

        val recoveredReminder = database.reminderDao().getReminderById(sampleReminder.id)

        assertThat(recoveredReminder, notNullValue())
        assertThat(recoveredReminder?.id, `is`(sampleReminder.id))
        assertThat(recoveredReminder?.title, `is`(sampleReminder.title))
        assertThat(recoveredReminder?.description, `is`(sampleReminder.description))
        assertThat(recoveredReminder?.location, `is`(sampleReminder.location))
        assertThat(recoveredReminder?.latitude, `is`(sampleReminder.latitude))
        assertThat(recoveredReminder?.longitude, `is`(sampleReminder.longitude))
    }

    @Test
    fun cleanDatabase_addRemindersAndDeleteReminders_ListShouldBeEmpty() = runBlockingTest {
        database.reminderDao().saveReminder(sampleReminder)

        val list = database.reminderDao().getReminders()
        assertThat(list.size, `is`(1))

        database.reminderDao().deleteAllReminders()
        val listAfterDelete = database.reminderDao().getReminders()
        assertThat(listAfterDelete.size, `is`(0))
    }
}
