package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
// END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() { // Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var viewModel: SaveReminderViewModel

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun setup() {
        stopKoin() // stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        // declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        // Get our real repository
        repository = get()
        viewModel = get()

        // clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun cleanUp() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun addReminderFlow() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(replaceText("Title1"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Description1"))
        onView(isRoot()).perform(closeSoftKeyboard())

        viewModel.latitude.postValue(FAKE_LATITUDE)
        viewModel.longitude.postValue(FAKE_LONGITUDE)
        viewModel.reminderSelectedLocationStr.postValue("Google Android Statues Square")

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText("Title1")).check(matches(isDisplayed()))
        onView(withText("Google Android Statues Square")).check(matches(isDisplayed()))
        onView(withId(R.id.reminderssRecyclerView)).check(matches(hasChildCount(1)))

        onView(withText("Google Android Statues Square")).perform(click())

        onView(withText("Title:")).check(matches(isDisplayed()))
        onView(withText("Title1")).check(matches(isDisplayed()))
        onView(withText("Description:")).check(matches(isDisplayed()))
        onView(withText("Description1")).check(matches(isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView)).check(doesNotExist())

        onView(withId(R.id.closeButton)).check(matches(isDisplayed()))
        onView(withId(R.id.closeButton)).perform(click())

        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    companion object {
        const val FAKE_LATITUDE = 37.4184
        const val FAKE_LONGITUDE = -122.0881
    }
}
