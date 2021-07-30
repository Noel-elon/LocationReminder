package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.FakeDataSource
import com.udacity.project4.util.atPosition
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert
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
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var fake: FakeDataSource

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin

        val remindersList = mutableListOf<ReminderDTO>()
        for (i in (1..10)) {
            val reminderDTO = ReminderDTO(
                "TitleOf$i",
                "DescriptionOf$i",
                "LocationOf$i",
                20.21,
                20.21
            )
            remindersList.add(reminderDTO)
        }

        fake = FakeDataSource(remindersList)

        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    fake
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

        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    //    TODO: add End to End testing to the app
    @Test
    fun launchReminderDescriptionScreenNotEmpty() {
        val reminder = ReminderDataItem("title", "description", "location", 00.0, 00.0)
        val intent = ReminderDescriptionActivity.newIntent(getApplicationContext(), reminder)
        val activityScenario = ActivityScenario.launch<ReminderDescriptionActivity>(intent)

        Espresso.onView(withId(R.id.title_txt)).check(
            ViewAssertions.matches(
                withText(
                    CoreMatchers.containsString(reminder.title)
                )
            )
        )

        Espresso.onView(withId(R.id.description_text)).check(
            ViewAssertions.matches(
                withText(
                    CoreMatchers.containsString(reminder.description)
                )
            )
        )

        Espresso.onView(withId(R.id.location_text)).check(
            ViewAssertions.matches(
                withText(
                    CoreMatchers.containsString(reminder.location)
                )
            )
        )
        activityScenario.close()
    }

    @Test
    fun createAReminder() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        val viewModel: SaveReminderViewModel = get()

        runBlocking {
            repository.deleteAllReminders()
        }
        //Load viewmodel with data to not require to test map
        viewModel.reminderSelectedLocationStr.postValue("Test")
        viewModel.latitude.postValue(0.0)
        viewModel.longitude.postValue(0.0)

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"))
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Description"), ViewActions.closeSoftKeyboard())


        onView(withId(R.id.saveReminder)).perform(ViewActions.click())


        //toast test
        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    `is`(
                        getActivity(activityScenario)?.window?.decorView
                    )
                )
            )
        ).check(matches(isDisplayed()))


        activityScenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it

        }
        return activity
    }

    @Test
    fun createReminderWithoutTitle() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        val viewModel: SaveReminderViewModel = get()
        //Load viewmodel with data to not require to test map
        viewModel.reminderSelectedLocationStr.postValue("Location Test")
        viewModel.latitude.postValue(0.0)
        viewModel.longitude.postValue(0.0)

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        //snack bar test
        onView(withText(R.string.err_enter_title))
            .check(ViewAssertions.matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun createReminderWithoutLocation() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        val viewModel: SaveReminderViewModel = get()

        viewModel.reminderTitle.postValue("Title")
        viewModel.latitude.postValue(0.0)
        viewModel.longitude.postValue(0.0)

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())


        //snack bar test
        onView(withText(R.string.err_select_location))
            .check(ViewAssertions.matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun testErrorLoadingReminder() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        val viewModel: RemindersListViewModel = get()

        runBlocking {
            fake.isError = true
        }
        onView(withId(R.id.refreshLayout)).perform(ViewActions.swipeDown())


        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("No reminders found")))


        activityScenario.close()
    }
}
