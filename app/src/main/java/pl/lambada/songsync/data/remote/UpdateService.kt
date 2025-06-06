package pl.lambada.songsync.data.remote

import android.content.Context
import kotlinx.coroutines.flow.flow
import pl.lambada.songsync.data.remote.github.GithubAPI
import pl.lambada.songsync.domain.model.Release
import pl.lambada.songsync.util.ext.getVersion

class UpdateService {

    /**
     * Checks for updates by comparing the latest release version with the current version.
     * @param context The context of the application.
     * @return A flow emitting the update state.
     */
    fun checkForUpdates(context: Context) = flow {
        emit(UpdateState.Checking)

        try {
            val latest = GithubAPI.getLatestRelease()
            val isUpdate = isNewerRelease(context, latest)

            emit(
                if (isUpdate)
                    UpdateState.UpdateAvailable(latest)
                else
                    UpdateState.UpToDate
            )

        } catch (e: Exception) {
            emit(UpdateState.Error(e))
        }
    }

    /**
     * Checks if the latest release is newer than the current version.
     * @param context The context of the application.
     * @param latestRelease The latest release from the GitHub API.
     * @return True if the latest release is newer, false otherwise.
     */
    private fun isNewerRelease(context: Context, latestRelease: Release): Boolean {
        val currentVersion = context
            .getVersion()
            .replace(".", "")
            .toInt()
        val latestVersion = latestRelease.tagName
            .replace(".", "")
            .replace("v", "")
            .toInt()

        return latestVersion > currentVersion
    }
}

/**
 * Defines the state of the update check.
 */
sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class UpdateAvailable(val release: Release) : UpdateState
    data class Error(val reason: Throwable) : UpdateState
}