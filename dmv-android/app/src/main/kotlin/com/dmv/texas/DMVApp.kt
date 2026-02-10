package com.dmv.texas

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.data.model.QuizConfig

class DMVApp : Application(), SingletonImageLoader.Factory {
    val database: DMVDatabase by lazy {
        DMVDatabase.getInstance(this)
    }

    /**
     * Temporary holder for [QuizConfig] to pass from HomeScreen to QuizViewModel
     * without serializing it into navigation route arguments. Set by HomeScreen
     * before navigating to the quiz flow; read and cleared by QuizViewModel on init.
     * Survives config changes (Application lives longer), but NOT process death
     * (which is acceptable -- quiz restarts from Home on process death).
     */
    @Volatile
    var pendingQuizConfig: QuizConfig? = null

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
}
