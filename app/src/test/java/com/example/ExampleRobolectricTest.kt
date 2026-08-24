package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.CalculatorEngine
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
    assertEquals("Calculator", appName)
  }

  @Test
  fun `basic arithmetic calculation`() {
    val res1 = CalculatorEngine.evaluate("2 + 3 * 4", CalculatorEngine.AngleMode.DEGREE)
    assertTrue(res1 is CalculatorEngine.EvalResult.Success)
    assertEquals("14", (res1 as CalculatorEngine.EvalResult.Success).formattedText)

    val res2 = CalculatorEngine.evaluate("(10 - 4) / 2", CalculatorEngine.AngleMode.DEGREE)
    assertTrue(res2 is CalculatorEngine.EvalResult.Success)
    assertEquals("3", (res2 as CalculatorEngine.EvalResult.Success).formattedText)
  }

  @Test
  fun `scientific operations degree vs radian`() {
    val sinDeg = CalculatorEngine.evaluate("sin(90)", CalculatorEngine.AngleMode.DEGREE)
    assertTrue(sinDeg is CalculatorEngine.EvalResult.Success)
    assertEquals("1", (sinDeg as CalculatorEngine.EvalResult.Success).formattedText)

    val cosDeg = CalculatorEngine.evaluate("cos(0)", CalculatorEngine.AngleMode.DEGREE)
    assertTrue(cosDeg is CalculatorEngine.EvalResult.Success)
    assertEquals("1", (cosDeg as CalculatorEngine.EvalResult.Success).formattedText)

    val sqrtVal = CalculatorEngine.evaluate("sqrt(144) + 2^3", CalculatorEngine.AngleMode.DEGREE)
    assertTrue(sqrtVal is CalculatorEngine.EvalResult.Success)
    assertEquals("20", (sqrtVal as CalculatorEngine.EvalResult.Success).formattedText)
  }

  @Test
  fun `factorial and percentage evaluation`() {
    val fact = CalculatorEngine.evaluate("5!", CalculatorEngine.AngleMode.DEGREE)
    assertTrue(fact is CalculatorEngine.EvalResult.Success)
    assertEquals("120", (fact as CalculatorEngine.EvalResult.Success).formattedText)

    val pct = CalculatorEngine.evaluate("50%", CalculatorEngine.AngleMode.DEGREE)
    assertTrue(pct is CalculatorEngine.EvalResult.Success)
    assertEquals("0.5", (pct as CalculatorEngine.EvalResult.Success).formattedText)
  }
}
