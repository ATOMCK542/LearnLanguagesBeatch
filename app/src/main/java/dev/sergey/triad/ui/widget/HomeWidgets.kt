package dev.sergey.triad.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider as DayNightColor
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.EntryPointAccessors
import dev.sergey.triad.R
import dev.sergey.triad.data.widget.WidgetCard
import dev.sergey.triad.di.WidgetEntryPoint
import dev.sergey.triad.domain.WidgetKind
import androidx.compose.ui.unit.dp

private val kindKey = ActionParameters.Key<String>(WidgetSessionActivity.EXTRA_KIND)

private val DayBg = Color(0xFFF7F3FB)
private val NightBg = Color(0xFF221C2E)
private val DayFg = Color(0xFF1C1628)
private val NightFg = Color(0xFFF3ECF8)
private val DayMuted = Color(0xFF4B4458)
private val NightMuted = Color(0xFFD8CFE6)
private val Accent = Color(0xFF8B7CB8)

abstract class KindGlanceWidget(private val kind: WidgetKind) : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val card = runCatching { load(context) }.getOrElse {
            WidgetCard(kind, context.getString(titleRes()), context.getString(R.string.error_generic), "")
        }
        provideContent { WidgetBody(card) }
    }

    private suspend fun load(context: Context): WidgetCard {
        val status = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        ).widgetStatus()
        return status.load(kind)
    }

    private fun titleRes(): Int = when (kind) {
        WidgetKind.Lesson -> R.string.widget_lesson_title
        WidgetKind.Review -> R.string.widget_review_title
    }
}

class LessonGlanceWidget : KindGlanceWidget(WidgetKind.Lesson)

class ReviewGlanceWidget : KindGlanceWidget(WidgetKind.Review)

class LessonWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LessonGlanceWidget()
}

class ReviewWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ReviewGlanceWidget()
}

@androidx.glance.GlanceComposable
@androidx.compose.runtime.Composable
private fun WidgetBody(card: WidgetCard) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(DayNightColor(DayBg, NightBg))
            .cornerRadius(20.dp)
            .padding(14.dp)
            .clickable(
                actionStartActivity<WidgetSessionActivity>(
                    actionParametersOf(kindKey to card.kind.name),
                ),
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            card.title,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DayNightColor(DayFg, NightFg),
            ),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            card.status,
            style = TextStyle(
                fontSize = 14.sp,
                color = DayNightColor(DayMuted, NightMuted),
            ),
            maxLines = 2,
        )
        if (card.detail.isNotBlank()) {
            Spacer(GlanceModifier.height(6.dp))
            Text(
                card.detail,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = ColorProvider(Accent),
                ),
            )
        }
    }
}
