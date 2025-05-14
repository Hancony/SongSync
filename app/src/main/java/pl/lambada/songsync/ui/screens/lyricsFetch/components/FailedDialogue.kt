package pl.lambada.songsync.ui.screens.lyricsFetch.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.util.EmptyQueryException
import pl.lambada.songsync.util.NoTrackFoundException
import java.io.FileNotFoundException

/**
 * Composable function to display a dialog for failed operations.
 *
 * @param onDismissRequest Callback to be invoked when the dialog is dismissed.
 * @param onOkRequest Callback to be invoked when the OK button is pressed.
 * @param exception The exception that caused the failure.
 */
@Composable
fun FailedDialogue(
    onDismissRequest: () -> Unit,
    onOkRequest: () -> Unit,
    exception: Exception
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { Button(onClick = onOkRequest) { Text(stringResource(R.string.ok)) } },
        title = { Text(text = stringResource(id = R.string.error)) },
        text = {
            when (exception) {
                is NoTrackFoundException -> Text(stringResource(R.string.no_results))
                is EmptyQueryException -> Text(stringResource(R.string.invalid_query))
                else -> Text(exception.toString())
            }
        }
    )
}