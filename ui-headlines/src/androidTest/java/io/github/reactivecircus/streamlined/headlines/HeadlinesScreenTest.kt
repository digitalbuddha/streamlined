package io.github.reactivecircus.streamlined.headlines

import androidx.test.filters.LargeTest
import io.github.reactivecircus.streamlined.headlines.di.HeadlinesTestAppComponent
import io.github.reactivecircus.streamlined.testing.BaseScreenTest
import org.junit.Test

@LargeTest
class HeadlinesScreenTest : BaseScreenTest() {

    private val fragmentFactory = HeadlinesTestAppComponent.getOrCreate().fragmentFactory

    @Test
    fun launchHeadlinesScreen_headlinesDisplayed() {
        launchFragmentScenario<HeadlinesFragment>(fragmentFactory)
        // TODO
    }
}
