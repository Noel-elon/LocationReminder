package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//TODO: Add testing implementation to the RemindersDao.kt
private lateinit var db: RemindersDatabase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initialize() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Test
    fun saveReminder() = runBlockingTest {
        val dao = db.reminderDao()
        val reminder = ReminderDTO(
            "TitleOf",
            "DescriptionOf",
            "LocationOf",
            20.21,
            20.21
        )
        dao.saveReminder(reminder)
        val fetchedReminder = dao.getReminderById(reminder.id)
        if (fetchedReminder != null) {
            ViewMatchers.assertThat(reminder.id, `is`(fetchedReminder.id))
            ViewMatchers.assertThat(reminder.title, `is`(fetchedReminder.title))
            ViewMatchers.assertThat(reminder.description, `is`(fetchedReminder.description))
            ViewMatchers.assertThat(reminder.location, `is`(fetchedReminder.location))
            ViewMatchers.assertThat(reminder.latitude, `is`(fetchedReminder.latitude))
            ViewMatchers.assertThat(reminder.longitude, `is`(fetchedReminder.longitude))
        }else Assert.fail()
    }
    @After
    fun closeDatabase() {
        db.close()
    }

}