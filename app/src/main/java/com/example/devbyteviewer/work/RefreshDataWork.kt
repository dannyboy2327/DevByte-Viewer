package com.example.devbyteviewer.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.devbyteviewer.database.VideosDatabase
import com.example.devbyteviewer.database.getDatabase
import com.example.devbyteviewer.repository.VideosRepository
import retrofit2.HttpException

class RefreshDataWork(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = VideosRepository(database)


        return try  {
            repository.refreshVideos()
            Result.success()
        } catch (exception: HttpException) {
            Result.retry()
        }
    }

}