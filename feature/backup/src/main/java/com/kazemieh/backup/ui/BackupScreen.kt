package com.kazemieh.backup.ui

import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun BackupScreen(
    viewModel: BackupViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackupEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is BackupEffect.ShareJson -> {
                    val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val appFolder = File(documentsDir, context.packageName)
                    if (!appFolder.exists()) {
                        appFolder.mkdirs()
                    }

                    val file = File(appFolder, "transactions.txt")
                    file.writeText(effect.json)
                    Toast.makeText(context, "Saved to ${file.path}", Toast.LENGTH_LONG).show()

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, effect.json)
                        type = "application/json"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Export JSON")
                    context.startActivity(shareIntent)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { viewModel.onEvent(BackupEvent.Export) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export JSON")
        }

        Spacer(modifier = Modifier.height(16.dp))

        var jsonInput by remember { mutableStateOf("") }
        OutlinedTextField(
            value = jsonInput,
            onValueChange = { jsonInput = it },
            label = { Text("Paste JSON to Import") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.onEvent(BackupEvent.Import(jsonInput)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import JSON")
        }
    }
}
