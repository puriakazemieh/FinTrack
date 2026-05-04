package com.kazemieh.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.kazemieh.designsystem.FintrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        startKoin {
//            androidContext(this@YourApplication)
//            modules(databaseModule, /* other modules */)
//        }
//        // اجرای مایگریشن
//        lifecycleScope.launch {
//            val migration = get<RoomToSQLDelightMigration>()
//            try {
//                migration.migrate()
//            } catch (e: MigrationException) {
//                // لاگ خطا یا نمایش به کاربر
//                Log.e("Migration", "Failed to migrate database", e)
//            }
//        }

        setContent {
            FintrackTheme {
                FinTrackHost()
            }
        }
    }
}
