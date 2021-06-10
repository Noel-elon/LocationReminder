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
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = CoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        val remindersList = mutableListOf<ReminderDTO>()
        for (i in (1..10)) {
            val reminderDTO = ReminderDTO(
                "Title$i",
                "Description$i",
                "Location$i",
                36.76,
                36.76
            )
            remindersList.add(reminderDTO)
        }

        fakeDataSource = FakeDataSource(remindersList)
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

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
    fun saveReminder_equalToDataSource() = mainCoroutineRule.runBlockingTest {

        fakeDataSource.deleteAllReminders()

        val reminder = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            36.79,
            37.45
        )
        saveReminderViewModel.saveReminder(reminder)
        val reminderFromDataSource = (fakeDataSource.getReminders() as Result.Success).data
        MatcherAssert.assertThat(
            reminder.title,
            CoreMatchers.equalTo(reminderFromDataSource[0].title)
        )
    }

    @Test
    fun saveReminder_loading() = mainCoroutineRule.runBlockingTest {

        val reminder = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            36.79,
            37.45
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
    fun saveReminder_toastMessage() = mainCoroutineRule.runBlockingTest {

        val reminder = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            36.79,
            37.45
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