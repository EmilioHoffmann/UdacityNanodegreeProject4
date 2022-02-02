package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    @Before
    fun setUp() {
        stopKoin()
        fakeDataSource = FakeDataSource()

        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @After
    fun cleanUp() = runBlocking {
        fakeDataSource.deleteAllReminders()
    }

    @Test
    fun addReminder_VerifySuccess_ShouldNavigateBack() = runBlockingTest {
        viewModel.saveReminder(
            ReminderDataItem(
                "Title1",
                "Description1",
                "Location1",
                11.11,
                11.11
            )
        )

        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertThat(viewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.Back))
    }

    @Test
    fun validateReminder_nullReminderFields_snackBarIntShouldBeEnterTitle() {
        val isReminderValid = viewModel.validateEnteredData(
            ReminderDataItem(
                null,
                null,
                null,
                null,
                null
            )
        )

        assertThat(isReminderValid, `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateReminder_emptyReminderFields_snackBarIntShouldBeEnterTitle() {
        val isReminderValid = viewModel.validateEnteredData(
            ReminderDataItem(
                "",
                "",
                "",
                null,
                null
            )
        )

        assertThat(isReminderValid, `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateReminder_emptyLocation_snackBarIntShouldBeSelectLocation() {
        val isReminderValid = viewModel.validateEnteredData(
            ReminderDataItem(
                "Title1",
                "",
                "",
                null,
                null
            )
        )

        assertThat(isReminderValid, `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }
}
