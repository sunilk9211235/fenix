/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.wallpapers

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.utils.Settings

/**
 * Provides access to available wallpapers and manages their states.
 */
@Suppress("TooManyFunctions")
class WallpaperManager(
    private val settings: Settings,
    private val appStore: AppStore,
    private val selectWallpaperUseCase: WallpapersUseCases.SelectWallpaperUseCase,
) {
    val logger = Logger("WallpaperManager")

    val wallpapers get() = appStore.state.wallpaperState.availableWallpapers
    val currentWallpaper: Wallpaper get() = appStore.state.wallpaperState.currentWallpaper

    /**
     * Returns the next available [Wallpaper], the [currentWallpaper] is the last one then
     * the first available [Wallpaper] will be returned.
     */
    fun switchToNextWallpaper(): Wallpaper {
        val values = wallpapers
        val index = values.indexOf(currentWallpaper) + 1

        return if (index >= values.size) {
            values.first()
        } else {
            values[index]
        }.also {
            selectWallpaperUseCase(it)
        }
    }

    /**
     * Animates the Firefox logo, if it hasn't been animated before, otherwise nothing will happen.
     * After animating the first time, the [Settings.shouldAnimateFirefoxLogo] setting
     * will be updated.
     */
    @Suppress("MagicNumber")
    fun animateLogoIfNeeded(logo: View) {
        if (!settings.shouldAnimateFirefoxLogo) {
            return
        }
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val animator1 = ObjectAnimator.ofFloat(logo, "rotation", 0f, 10f)
                val animator2 = ObjectAnimator.ofFloat(logo, "rotation", 10f, 0f)
                val animator3 = ObjectAnimator.ofFloat(logo, "rotation", 0f, 10f)
                val animator4 = ObjectAnimator.ofFloat(logo, "rotation", 10f, 0f)

                animator1.duration = 200
                animator2.duration = 200
                animator3.duration = 200
                animator4.duration = 200

                val set = AnimatorSet()

                set.play(animator1).before(animator2).after(animator3).before(animator4)
                set.start()

                settings.shouldAnimateFirefoxLogo = false
            },
            ANIMATION_DELAY_MS
        )
    }

    companion object {
        /**
         *  Get whether the default wallpaper should be used.
         */
        fun isDefaultTheCurrentWallpaper(settings: Settings): Boolean = with(settings.currentWallpaper) {
            return isEmpty() || equals(defaultWallpaper.name)
        }

        val defaultWallpaper = Wallpaper.Default
        private const val ANIMATION_DELAY_MS = 1500L
    }
}
