package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val dataSource: MutableList<ReminderDTO>) : ReminderDataSource {

    var isError: Boolean = false

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if(isError)
            Result.Error("No reminders found")
        else
            Result.Success(dataSource)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {

        dataSource.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        for(reminder in dataSource)
            if(reminder.id == id)
                return Result.Success(reminder)
        return Result.Error("Not found")
    }

    override suspend fun deleteAllReminders() {
        dataSource.clear()
    }


}