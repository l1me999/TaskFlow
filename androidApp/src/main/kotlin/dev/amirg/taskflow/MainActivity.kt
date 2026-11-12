package dev.amirg.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.amirg.taskflow.data.TaskStore
import dev.amirg.taskflow.ui.App
import okio.Path.Companion.toOkioPath

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = TaskStore(filesDir.resolve("taskflow.json").toOkioPath())

        setContent {
            App(store)
        }
    }
}
