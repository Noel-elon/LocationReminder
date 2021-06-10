package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
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

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = CoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        val remindersList = mutableListOf<ReminderDTO>()
        for(i in (1 ..10)){
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
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun loadReminder_checkNotNull() {
        // GIVEN
        remindersListViewModel.loadReminders()

        // WHEN
        val viewModelList = remindersListViewModel.remindersList.getOrAwaitValue()

        // THEN
        MatcherAssert.assertThat(viewModelList, CoreMatchers.not(CoreMatchers.nullValue()))
    }

    @Test
    fun loadReminder_compareViewmodelWithDatasourceReminders() = mainCoroutineRule.runBlockingTest {

        remindersListViewModel.loadReminders()
        val reminderFromViewModel = remindersListViewModel.remindersList.getOrAwaitValue()
        val reminderFromDataSource = (fakeDataSource.getReminders() as Result.Success).data

        MatcherAssert.assertThat(
            reminderFromViewModel[0].id,
            CoreMatchers.`is`(reminderFromDataSource[0].id)
        )
        MatcherAssert.assertThat(
            reminderFromViewModel[0].title,
            CoreMatchers.`is`(reminderFromDataSource[0].title)
        )
        MatcherAssert.assertThat(
            reminderFromViewModel[0].description,
            CoreMatchers.`is`(reminderFromDataSource[0].description)
        )
        MatcherAssert.assertThat(
            reminderFromViewModel[0].location,
            CoreMatchers.`is`(reminderFromDataSource[0].location)
        )
        MatcherAssert.assertThat(
            reminderFromViewModel[0].longitude,
            CoreMatchers.`is`(reminderFromDataSource[0].longitude)
        )
        MatcherAssert.assertThat(
            reminderFromViewModel[0].latitude,
            CoreMatchers.`is`(reminderFromDataSource[0].latitude)
        )

    }

    @Test
    fun loadReminder_loading() {
        mainCoroutineRule.pauseDispatcher()

        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadReminder_whenUnavaibleOrEmpty() {
        fakeDataSource.shouldReturnError = true

        remindersListViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            CoreMatchers.`is`("Reminders not found")
        )
    }

}