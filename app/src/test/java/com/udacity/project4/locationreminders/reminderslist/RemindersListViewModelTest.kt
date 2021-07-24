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
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var fakeSource: FakeDataSource

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = CoroutineRule()

    @Before
    fun initializeReminders() {
        stopKoin()
        val remindersList = mutableListOf<ReminderDTO>()
        for(i in (1 ..10)){
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
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeSource)
    }

    @Test
    fun loadReminder_checkNotNull() {
        viewModel.loadReminders()

        val viewModelList = viewModel.remindersList.getOrAwaitValue()

        MatcherAssert.assertThat(viewModelList, CoreMatchers.not(CoreMatchers.nullValue()))
    }

    @Test
    fun loadReminder_compareReminders() = mainCoroutineRule.runBlockingTest {

        viewModel.loadReminders()
        val reminderFromViewModel = viewModel.remindersList.getOrAwaitValue()
        val reminderFromDataSource = (fakeSource.getReminders() as Result.Success).data

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
    fun loadReminder_isLoading() {
        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()
        MatcherAssert.assertThat(
            viewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            viewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadReminder_ifEmpty() {
        fakeSource.isError = true
        viewModel.loadReminders()
        MatcherAssert.assertThat(
            viewModel.showSnackBar.getOrAwaitValue(),
            CoreMatchers.`is`("No reminders found")
        )
    }

}