package ru.parcel.app.nav.vectorres

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Package24: ImageVector
    get() {
        if (package24 != null) {
            return package24!!
        }
        package24 = ImageVector.Builder(
            name = "Package224",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(440f, 777f)
                lineTo(440f, 503f)
                lineTo(200f, 364f)
                lineTo(200f, 638f)
                quadTo(200f, 638f, 200f, 638f)
                quadTo(200f, 638f, 200f, 638f)
                lineTo(440f, 777f)
                close()
                moveTo(520f, 777f)
                lineTo(760f, 638f)
                quadTo(760f, 638f, 760f, 638f)
                quadTo(760f, 638f, 760f, 638f)
                lineTo(760f, 364f)
                lineTo(520f, 503f)
                lineTo(520f, 777f)
                close()
                moveTo(440f, 869f)
                lineTo(160f, 708f)
                quadTo(141f, 697f, 130.5f, 679f)
                quadTo(120f, 661f, 120f, 639f)
                lineTo(120f, 321f)
                quadTo(120f, 299f, 130.5f, 281f)
                quadTo(141f, 263f, 160f, 252f)
                lineTo(440f, 91f)
                quadTo(459f, 80f, 480f, 80f)
                quadTo(501f, 80f, 520f, 91f)
                lineTo(800f, 252f)
                quadTo(819f, 263f, 829.5f, 281f)
                quadTo(840f, 299f, 840f, 321f)
                lineTo(840f, 639f)
                quadTo(840f, 661f, 829.5f, 679f)
                quadTo(819f, 697f, 800f, 708f)
                lineTo(520f, 869f)
                quadTo(501f, 880f, 480f, 880f)
                quadTo(459f, 880f, 440f, 869f)
                close()
                moveTo(640f, 341f)
                lineTo(717f, 297f)
                lineTo(480f, 160f)
                quadTo(480f, 160f, 480f, 160f)
                quadTo(480f, 160f, 480f, 160f)
                lineTo(402f, 205f)
                lineTo(640f, 341f)
                close()
                moveTo(480f, 434f)
                lineTo(558f, 389f)
                lineTo(321f, 252f)
                lineTo(243f, 297f)
                lineTo(480f, 434f)
                close()
            }
        }.build()

        return package24!!
    }

private var package24: ImageVector? = null
