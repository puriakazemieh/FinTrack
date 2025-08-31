package com.kazemieh.category.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun CategoryListScreen(
    viewModel: CategoryViewModel = koinViewModel(),
    onAddCategoryClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onAddCategoryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("افزودن دسته‌بندی")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(state.categories) { category ->
                Text(
                    text = category.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}
