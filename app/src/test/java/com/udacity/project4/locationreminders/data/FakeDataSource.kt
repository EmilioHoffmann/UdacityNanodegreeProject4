package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

// Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private val fakeData = hashMapOf<String, ReminderDTO>()
    var returnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (returnError) {
            Result.Error("Data error")
        } else {
            Result.Success(fakeData.values.toList())
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        fakeData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = fakeData[id]
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder not found with id $id")
        }
    }

    override suspend fun deleteAllReminders() {
        fakeData.clear()
    }
}
