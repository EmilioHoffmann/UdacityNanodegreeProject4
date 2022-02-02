package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var dataSource: FakeDataSource

    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setup() {
        stopKoin()
        val applicationMock = Mockito.mock(Application::class.java)
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(applicationMock, dataSource)
    }

    @After
    fun cleanUp() = runBlocking {
        dataSource.deleteAllReminders()
    }

    private val fakeRemindersList = arrayListOf(
        ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            11.11,
            11.11
        ),
        ReminderDTO(
            "Title2",
            "Description2",
            "Location2",
            22.22,
            22.22
        ),
        ReminderDTO(
            "Title3",
            "Description3",
            "Location3",
            33.33,
            33.33
        )
    )

    @Test
    fun loadReminders_SaveList_returnListSameSize() = runBlockingTest {
        fakeRemindersList.forEach { reminderDTO ->
            dataSource.saveReminder(reminderDTO)
        }

        viewModel.loadReminders()

        assertThat(
            viewModel.remindersList.getOrAwaitValue().size,
            `is`(fakeRemindersList.size)
        )
    }

    @Test
    fun loadReminders_WithoutSave_returnEmptyList() = runBlockingTest {
        viewModel.loadReminders()

        assertThat(
            viewModel.remindersList.getOrAwaitValue(),
            `is`(emptyList())
        )
    }

    @Test
    fun loadReminders_SaveList_Error() = runBlockingTest {
        dataSource.returnError = true

        viewModel.loadReminders()

        assertThat(
            viewModel.showSnackBar.getOrAwaitValue(),
            `is`("Data error")
        )
    }
}
