package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@LargeTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
private lateinit var repo: ReminderDataSource
    private lateinit var context: Application


    private val reminder = ReminderDTO(
        "TitleOf",
        "DescriptionOf",
        "LocationOf",
        20.21,
        20.21
    )
    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun initialize() {
        stopKoin()
        context = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(context) }
        }


        startKoin {
            modules(listOf(myModule))
        }

        repo = get()

        runBlocking {
            repo.deleteAllReminders()
        }
    }

    @After
    fun stop() {
        stopKoin()
    }

    @Test
    fun clearAll_checkNoDataDisplayed() {

        runBlocking {
            repo.saveReminder(reminder)
        }

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        runBlocking {
            repo.deleteAllReminders()
        }

        Espresso.onView(ViewMatchers.withId(R.id.refreshLayout)).perform(ViewActions.swipeDown())
        Espresso.onView(ViewMatchers.withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun navigateToSaveReminderOnFabClick() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }


    @Test
    fun checkIfReminderDisplays() {

        runBlocking {
            repo.saveReminder(reminder)
        }

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        Espresso.onView(ViewMatchers.withText(reminder.title))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(reminder.description))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    }


}