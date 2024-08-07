package ru.parcel.app.nav.vectorres

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Archive24: ImageVector
    get() {
        if (archive24 != null) {
            return archive24!!
        }
        archive24 = ImageVector.Builder(
            name = "Archive24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(480f, 720f)
                lineTo(640f, 560f)
                lineTo(584f, 504f)
                lineTo(520f, 568f)
                lineTo(520f, 400f)
                lineTo(440f, 400f)
                lineTo(440f, 568f)
                lineTo(376f, 504f)
                lineTo(320f, 560f)
                lineTo(480f, 720f)
                close()
                moveTo(200f, 320f)
                lineTo(200f, 760f)
                quadTo(200f, 760f, 200f, 760f)
                quadTo(200f, 760f, 200f, 760f)
                lineTo(760f, 760f)
                quadTo(760f, 760f, 760f, 760f)
                quadTo(760f, 760f, 760f, 760f)
                lineTo(760f, 320f)
                lineTo(200f, 320f)
                close()
                moveTo(200f, 840f)
                quadTo(167f, 840f, 143.5f, 816.5f)
                quadTo(120f, 793f, 120f, 760f)
                lineTo(120f, 261f)
                quadTo(120f, 247f, 124.5f, 234f)
                quadTo(129f, 221f, 138f, 210f)
                lineTo(188f, 149f)
                quadTo(199f, 135f, 215.5f, 127.5f)
                quadTo(232f, 120f, 250f, 120f)
                lineTo(710f, 120f)
                quadTo(728f, 120f, 744.5f, 127.5f)
                quadTo(761f, 135f, 772f, 149f)
                lineTo(822f, 210f)
                quadTo(831f, 221f, 835.5f, 234f)
                quadTo(840f, 247f, 840f, 261f)
                lineTo(840f, 760f)
                quadTo(840f, 793f, 816.5f, 816.5f)
                quadTo(793f, 840f, 760f, 840f)
                lineTo(200f, 840f)
                close()
                moveTo(216f, 240f)
                lineTo(744f, 240f)
                lineTo(710f, 200f)
                quadTo(710f, 200f, 710f, 200f)
                quadTo(710f, 200f, 710f, 200f)
                lineTo(250f, 200f)
                quadTo(250f, 200f, 250f, 200f)
                quadTo(250f, 200f, 250f, 200f)
                lineTo(216f, 240f)
                close()
                moveTo(480f, 540f)
                lineTo(480f, 540f)
                lineTo(480f, 540f)
                quadTo(480f, 540f, 480f, 540f)
                quadTo(480f, 540f, 480f, 540f)
                lineTo(480f, 540f)
                quadTo(480f, 540f, 480f, 540f)
                quadTo(480f, 540f, 480f, 540f)
                lineTo(480f, 540f)
                close()
            }
        }.build()

        return archive24!!
    }

private var archive24: ImageVector? = null
