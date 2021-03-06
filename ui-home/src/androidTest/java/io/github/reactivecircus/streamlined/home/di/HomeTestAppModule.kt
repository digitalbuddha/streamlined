package io.github.reactivecircus.streamlined.home.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.multibindings.IntoMap
import io.github.reactivecircus.streamlined.home.HomeFragment
import io.github.reactivecircus.streamlined.home.HomeUiConfigs
import io.github.reactivecircus.streamlined.home.TestHomeUiConfigs
import io.github.reactivecircus.streamlined.navigator.NavigatorProvider
import io.github.reactivecircus.streamlined.testing.NoOpNavigatorProvider
import io.github.reactivecircus.streamlined.ui.di.DynamicFragmentFactory
import io.github.reactivecircus.streamlined.ui.di.FragmentKey
import kotlin.time.ExperimentalTime

@Module
abstract class HomeTestAppModule {

    @Binds
    @Reusable
    abstract fun fragmentFactory(impl: DynamicFragmentFactory): FragmentFactory

    @Binds
    @Reusable
    abstract fun navigatorProvider(impl: NoOpNavigatorProvider): NavigatorProvider

    @Binds
    @Reusable
    @OptIn(ExperimentalTime::class)
    abstract fun homeUiConfigs(impl: TestHomeUiConfigs): HomeUiConfigs

    @Binds
    @IntoMap
    @FragmentKey(HomeFragment::class)
    abstract fun homeFragment(fragment: HomeFragment): Fragment
}
