package dev.sergey.triad

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.sergey.triad.ui.TriadRoot
import dev.sergey.triad.ui.theme.TriadTheme
import dev.sergey.triad.ui.widget.LessonGlanceWidget
import dev.sergey.triad.ui.widget.ReviewGlanceWidget
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            TriadTheme {
                TriadRoot()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            LessonGlanceWidget().updateAll(this@MainActivity)
            ReviewGlanceWidget().updateAll(this@MainActivity)
        }
    }
}
