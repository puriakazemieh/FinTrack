package com.kazemieh.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kazemieh.dashboard.DashboardScreen
import com.kazemieh.financialsource.ui.FinancialSourceListScreen
import com.kazemieh.financialsource.ui.add.AddFinancialSourceBottomSheet
import com.kazemieh.fintrack.ui.theme.FinTrackTheme
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
                        var showAddSource by remember { mutableStateOf(false) }
                        var financialSourceList by remember { mutableStateOf(false) }
                        var showAddTransaction by remember { mutableStateOf(false) }
                        var addedFinancialSource by remember {
                            mutableStateOf<Pair<Int?, String?>>(null to null)
                        }
//                        TransactionScreen()
//                        AddTransactionScreen{}
//                        AddCategoryScreen{}
//                        CategoryListScreen{}
//                        FinancialSourceListScreen{}
//                        AddFinancialSourceScreen{}
//                        AddTagScreen {}
//                        TagListScreen {}

                        DashboardScreen(
                            onNavigateAddSource = {
                                showAddSource = true
                            },
                            onNavigateAddTransaction = {
                                showAddTransaction = true
                            },
                            onShowAllTransaction = {
                                TransactionScreen()
                            }
                        )
                        if (showAddSource) {
                            AddFinancialSourceBottomSheet(
                                onDismiss = {
                                    showAddSource = false
                                },
                                onFinancialSourceAdded = { id, name ->
                                    addedFinancialSource = id to name
                                    showAddSource = false
                                }
                            )
                        }

                        if (showAddTransaction) {
                            AddTransactionScreen(
                                addedFinancialSource = addedFinancialSource,
                                onDismiss = {
                                    addedFinancialSource = null to null
                                    showAddTransaction = false
                                },
                                onFinancialSourceAdded = {
                                    showAddTransaction = false
                                },
                                onFinancialSourceClicked = {
                                    addedFinancialSource = null to null
                                    financialSourceList = true
                                }
                            )
                        }

                        if (financialSourceList) {
                            FinancialSourceListScreen(
                                onAddCategoryClick = {
                                    showAddSource = true
                                    financialSourceList = false
                                },
                                onCategoryClick = { id, name ->
                                    addedFinancialSource = id to name
                                    financialSourceList = false
                                },
                                onDismiss = {
                                    addedFinancialSource = null to null
                                    financialSourceList = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
