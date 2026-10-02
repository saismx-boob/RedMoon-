package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.content.FighterRegistry
import com.example.game.core.ComboTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kairo", appName)
    }

    @Test
    fun `verify fighter registry contains four fighters`() {
        assertEquals(4, FighterRegistry.ALL_FIGHTERS.size)
        val kairo = FighterRegistry.getFighter("kairo")
        assertEquals("Kairo", kairo.name)
        assertTrue(kairo.maxHealth >= 90f)
    }

    @Test
    fun `verify combo damage scaling and rating`() {
        val tracker = ComboTracker()
        val hit1 = tracker.registerHit(10f, false)
        assertEquals(10f, hit1, 0.01f)
        assertEquals(1, tracker.hitCount)

        val hit2 = tracker.registerHit(10f, false)
        assertEquals(9f, hit2, 0.01f) // 90% scaling
        assertEquals(2, tracker.hitCount)
        assertEquals("GOOD COMBO!", tracker.ratingText)
    }
}
