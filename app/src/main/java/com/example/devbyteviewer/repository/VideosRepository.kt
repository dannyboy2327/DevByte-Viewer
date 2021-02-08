package com.example.devbyteviewer.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.devbyteviewer.database.VideosDatabase
import com.example.devbyteviewer.database.asDomainModel
import com.example.devbyteviewer.domain.Video
import com.example.devbyteviewer.network.Network
import com.example.devbyteviewer.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for fetching devbyte videos from the network and storing them on disk.
 */
class VideosRepository(private val database: VideosDatabase) {

    /*
     *A playlist of videos that can be show on the screen
     */
    val videos: LiveData<List<Video>> = Transformations.map(database.videoDao.getVideos()) {
        it.asDomainModel()
    }

    /**
     * Refresh the videos stored in the offline cache
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher using 'withContext' this function is now safe to call
     * from any thread including the main thread
     */
    suspend fun refreshVideos() {
        withContext(Dispatchers.IO) {
            val playList = Network.devbytes.getPlaylist().await()
            database.videoDao.insertAll(*playList.asDatabaseModel())
        }
    }
}