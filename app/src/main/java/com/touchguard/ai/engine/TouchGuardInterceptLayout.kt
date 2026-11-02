package com.touchguard.ai.engine

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.touchguard.ai.core.model.FilteredTouchEvent

class TouchGuardInterceptLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var engine: TouchAnalysisEngine? = null
    var isFilterActive: Boolean = true
    var onEventFiltered: ((FilteredTouchEvent) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        engine?.updateScreenDimensions(w, h)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val eng = engine
        if (!isFilterActive || eng == null) {
            return super.dispatchTouchEvent(ev)
        }

        val shouldSuppress = eng.processMotionEvent(ev)
        if (shouldSuppress) {
            // Drop ghost touch event from propagating to child buttons / list views
            // We can also synthesize an ACTION_CANCEL to reset child states if needed
            return true // Consumed / Suppressed
        }

        return super.dispatchTouchEvent(ev)
    }
}
