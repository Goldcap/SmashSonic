package com.smashsonic.data.repository

import android.content.Context
import com.smashsonic.data.local.DownloadedSongDao
import com.smashsonic.data.local.DownloadedSongEntity
import com.smashsonic.data.model.Song
import com.smashsonic.data.remote.SubsonicUrlBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: DownloadedSongDao,
    private val urlBuilder: SubsonicUrlBuilder,
    private val okHttpClient: OkHttpClient,
) {
    private val downloadsDir: File
        get() = File(context.filesDir, "downloads").also { it.mkdirs() }

    val downloadedSongs: Flow<List<DownloadedSongEntity>> = dao.getAll()

    private val _activeDownloads = MutableStateFlow<Map<String, Float>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, Float>> = _activeDownloads.asStateFlow()

    suspend fun isDownloaded(songId: String): Boolean = dao.exists(songId)

    fun localPath(songId: String): String? {
        val dir = downloadsDir
        return dir.listFiles()?.firstOrNull { it.name.startsWith(songId) }?.absolutePath
    }

    suspend fun download(song: Song) {
        val url = urlBuilder.downloadUrl(song.id) ?: return
        if (_activeDownloads.value.containsKey(song.id)) return

        _activeDownloads.value = _activeDownloads.value + (song.id to 0f)

        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()
                val body = response.body ?: return@withContext

                val suffix = song.suffix ?: "mp3"
                val file = File(downloadsDir, "${song.id}.$suffix")
                val totalBytes = body.contentLength()

                file.outputStream().use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = 0L
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (totalBytes > 0) {
                                _activeDownloads.value = _activeDownloads.value + (song.id to (totalRead.toFloat() / totalBytes))
                            }
                        }
                    }
                }

                dao.insert(DownloadedSongEntity.from(song, file.absolutePath))
            } catch (e: Exception) {
                // Clean up on failure
            } finally {
                _activeDownloads.value = _activeDownloads.value - song.id
            }
        }
    }

    suspend fun deleteDownload(songId: String) {
        dao.getById(songId)?.let { entity ->
            File(entity.localPath).delete()
        }
        // Also try by prefix
        downloadsDir.listFiles()?.filter { it.name.startsWith(songId) }?.forEach { it.delete() }
        dao.delete(songId)
    }
}
