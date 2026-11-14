package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BloatwareCatalog
import com.example.data.SafetyLevel
import org.junit.Assert.assertEquals
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
    assertEquals("Y5 Debloater", appName)
  }

  @Test
  fun `bloatware catalog classifies safe and critical packages`() {
    assertEquals(SafetyLevel.SAFE_BLOAT, BloatwareCatalog.analyze("com.huawei.appmarket").level)
    assertEquals(SafetyLevel.CRITICAL, BloatwareCatalog.analyze("com.android.systemui").level)
    assertEquals(SafetyLevel.CAUTION, BloatwareCatalog.analyze("com.google.android.gms").level)
  }
}
