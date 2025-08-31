package com.kazemieh.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kazemieh.backup.ui.BackupScreen
import com.kazemieh.category.ui.CategoryListScreen
import com.kazemieh.category.ui.add.AddCategoryScreen
import com.kazemieh.financialsource.ui.FinancialSourceListScreen
import com.kazemieh.financialsource.ui.add.AddFinancialSourceScreen
import com.kazemieh.fintrack.ui.theme.FinTrackTheme
import com.kazemieh.tag.ui.TagListScreen
import com.kazemieh.tag.ui.add.AddTagScreen
import com.kazemieh.transaction.ui.TransactionScreen
import com.kazemieh.transaction.ui.add.AddTransactionScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
//                        TransactionScreen()
//                        AddTransactionScreen{}
//                        AddCategoryScreen{}
//                        CategoryListScreen{}
//                        FinancialSourceListScreen{}
//                        AddFinancialSourceScreen{}
//                        AddTagScreen {}
//                        TagListScreen {}
                        BackupScreen()
                    }
                }
            }
        }
    }
}
