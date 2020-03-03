package io.github.reactivecircus.streamlined.home

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.github.reactivecircus.analytics.AnalyticsApi
import io.github.reactivecircus.streamlined.design.setDefaultBackgroundColor
import io.github.reactivecircus.streamlined.domain.model.Story
import io.github.reactivecircus.streamlined.home.databinding.FragmentHomeBinding
import io.github.reactivecircus.streamlined.navigator.NavigatorProvider
import io.github.reactivecircus.streamlined.ui.base.BaseFragment
import io.github.reactivecircus.streamlined.ui.configs.AnimationConfigs
import io.github.reactivecircus.streamlined.ui.viewmodel.fragmentViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.swiperefreshlayout.refreshes
import javax.inject.Inject
import javax.inject.Provider
import io.github.reactivecircus.streamlined.ui.R as CommonUiResource

@UseExperimental(ExperimentalCoroutinesApi::class)
class HomeFragment @Inject constructor(
    analyticsApi: AnalyticsApi,
    private val navigatorProvider: NavigatorProvider,
    private val viewModelProvider: Provider<HomeViewModel>,
    private val animationConfigs: AnimationConfigs
) : BaseFragment(R.layout.fragment_home, analyticsApi) {

    private val viewModel: HomeViewModel by fragmentViewModel { viewModelProvider.get() }

    private lateinit var feedsListAdapter: FeedsListAdapter

    private var errorSnackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)

        binding.toolbar.title = getString(R.string.title_home)

        binding.swipeRefreshLayout.refreshes()
            .onEach { viewModel.refreshHomeFeeds() }
            .launchIn(lifecycleScope)

        binding.retryButton.clicks()
            .onEach { viewModel.refreshHomeFeeds() }
            .launchIn(lifecycleScope)

        feedsListAdapter = FeedsListAdapter(
            actionListener = actionListener,
            animationConfigs = if (savedInstanceState == null) animationConfigs else null
        )

        binding.homeFeedsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = feedsListAdapter
        }

        viewModel.state.observe<HomeState>(viewLifecycleOwner) { state ->
            when (state) {
                is HomeState.Idle -> binding.showIdleState()
                is HomeState.InFlight -> binding.showInFlightState(state.items)
                is HomeState.Error -> binding.showErrorState()
            }
            state.items?.run {
                feedsListAdapter.submitList(this)
            }
        }

        viewModel.effect
            .filterIsInstance<HomeEffect.ShowTransientError>()
            .onEach { binding.showErrorSnackbarOnce() }
            .launchIn(lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        errorSnackbar?.dismiss()
    }

    private fun FragmentHomeBinding.showIdleState() {
        errorStateView.isVisible = false
        progressBar.isVisible = false
        swipeRefreshLayout.isRefreshing = false
        swipeRefreshLayout.isEnabled = true
        homeFeedsRecyclerView.isVisible = true
    }

    private fun FragmentHomeBinding.showInFlightState(items: List<FeedItem>?) {
        errorStateView.isVisible = false
        progressBar.isVisible = items == null
        swipeRefreshLayout.isRefreshing = items != null
        swipeRefreshLayout.isEnabled = items != null
        homeFeedsRecyclerView.isVisible = items != null
        errorSnackbar?.dismiss()
    }

    private fun FragmentHomeBinding.showErrorState() {
        errorStateView.isVisible = true
        progressBar.isVisible = false
        swipeRefreshLayout.isRefreshing = false
        swipeRefreshLayout.isEnabled = false
        homeFeedsRecyclerView.isVisible = false
        errorSnackbar?.dismiss()
    }

    private fun FragmentHomeBinding.showErrorSnackbarOnce() {
        if (errorSnackbar?.isShownOrQueued != true) {
            val errorMessage = getString(
                CommonUiResource.string.error_message_could_not_refresh_content
            )
            errorSnackbar = Snackbar
                .make(root, errorMessage, Snackbar.LENGTH_LONG)
                .setDefaultBackgroundColor()
                .apply { show() }
        }
    }

    private val actionListener = object : FeedsListAdapter.ActionListener {
        // TODO
        override fun storyClicked(story: Story) = Unit

        // TODO
        override fun bookmarkToggled(story: Story) = Unit

        // TODO
        override fun moreButtonClicked(story: Story) = Unit

        override fun readMoreHeadlinesButtonClicked() {
            navigatorProvider.get().navigateToHeadlinesScreen()
        }
    }
}
