package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CurveDirection
import com.example.model.ThrowPhysics
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
    assertEquals("RegiBot", appName)
  }

  @Test
  fun `curveball trajectory calculates valid points`() {
    val points = ThrowPhysics.calculateCurveballTrajectory(
      curveDirection = CurveDirection.COUNTER_CLOCKWISE,
      powerBoost = 1.0f
    )
    assertTrue("Trajectory should contain sample points", points.isNotEmpty())
    assertEquals("Start point should be bottom center", 0.5f, points.first().x, 0.05f)
  }
}

