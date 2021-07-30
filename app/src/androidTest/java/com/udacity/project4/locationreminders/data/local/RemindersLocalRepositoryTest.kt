package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
private lateinit var repo: RemindersLocalRepository
private lateinit var db: RemindersDatabase


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val reminder = ReminderDTO(
        "TitleOf",
        "DescriptionOf",
        "LocationOf",
        20.21,
        20.21
    )

    @Before
    fun initialize() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repo = RemindersLocalRepository(db.reminderDao(), Dispatchers.Main)
    }


    @Test
    fun checkIfDataIsCleared() = runBlocking {

        repo.saveReminder(reminder)
        repo.deleteAllReminders()
        val repoReminders = (repo.getReminders() as Result.Success).data

        assertThat(repoReminders, `is`(emptyList()))
    }

    @Test
    fun compareDataToRepo() =
        runBlocking {
        repo.saveReminder(reminder)

        val loaded = (repo.getReminder(reminder.id) as Result.Success).data

        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.location, `is`(reminder.location))

    }

    @Test
    fun checkReminderNotFound() =
        runBlocking {
            repo.saveReminder(reminder)
            val message = (repo.getReminder("noel") as Result.Error).message
            assertThat(message, `is`("Reminder not found!"))

        }


    @After
    fun closeDatabase() {
        db.close()
    }

}