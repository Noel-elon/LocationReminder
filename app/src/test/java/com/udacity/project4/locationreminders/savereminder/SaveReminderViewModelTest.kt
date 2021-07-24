package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.testutils.CoroutineRule
import com.udacity.project4.locationreminders.testutils.getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeSource: FakeDataSource

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = CoroutineRule()

    @Before
    fun initialize() {
        stopKoin()
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

        fakeSource = FakeDataSource(remindersList)
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeSource)

    }

    @Test
    fun onClear_isNull() {
        saveReminderViewModel.onClear()

        MatcherAssert.assertThat(
            saveReminderViewModel.reminderTitle.getOrAwaitValue(),
            CoreMatchers.nullValue()
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.reminderDescription.getOrAwaitValue(),
            CoreMatchers.nullValue()
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),
            CoreMatchers.nullValue()
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.selectedPOI.getOrAwaitValue(),
            CoreMatchers.nullValue()
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.latitude.getOrAwaitValue(),
            CoreMatchers.nullValue()
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.longitude.getOrAwaitValue(),
            CoreMatchers.nullValue()
        )

    }

    @Test
    fun saveReminder_compareWithDataSource() = mainCoroutineRule.runBlockingTest {

        fakeSource.deleteAllReminders()

        val reminder = ReminderDataItem(
            "TitleOfu",
            "DescriptionOfu",
            "LocationOfu",
            20.22,
            20.22
        )
        saveReminderViewModel.saveReminder(reminder)
        val reminderFromDataSource = (fakeSource.getReminders() as Result.Success).data
        MatcherAssert.assertThat(
            reminder.title,
            CoreMatchers.equalTo(reminderFromDataSource[0].title)
        )
    }

    @Test
    fun saveReminder_loading() = mainCoroutineRule.runBlockingTest {

        val reminder = ReminderDataItem(
            "TitleOf",
            "DescriptionOf",
            "LocationOf",
            20.21,
            20.22
        )
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )

    }

    @Test
    fun saveReminder_testToastMessage() = mainCoroutineRule.runBlockingTest {

        val reminder = ReminderDataItem(
            "TitleOf",
            "DescriptionOf",
            "LocationOf",
            20.21,
            20.21
        )

        saveReminderViewModel.saveReminder(reminder)
        MatcherAssert.assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(), CoreMatchers.`is`(
                ApplicationProvider.getApplicationContext<Context>()
                    .getString(R.string.reminder_saved)
            )
        )

    }


}