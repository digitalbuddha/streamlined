package io.github.reactivecircus.streamlined.data.di

import android.content.Context
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.github.reactivecircus.streamlined.data.mapper.toEntity
import io.github.reactivecircus.streamlined.data.mapper.toModel
import io.github.reactivecircus.streamlined.data.repository.BookmarkRepositoryImpl
import io.github.reactivecircus.streamlined.data.repository.StoryRepositoryImpl
import io.github.reactivecircus.streamlined.domain.model.Story
import io.github.reactivecircus.streamlined.domain.repository.BookmarkRepository
import io.github.reactivecircus.streamlined.domain.repository.StoryRepository
import io.github.reactivecircus.streamlined.persistence.StoryDao
import io.github.reactivecircus.streamlined.persistence.StoryEntity
import io.github.reactivecircus.streamlined.persistence.di.PersistenceComponent
import io.github.reactivecircus.streamlined.remote.api.NewsApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map

@Module(includes = [DataModule.Providers::class])
internal abstract class DataModule {

    @Binds
    @Reusable
    abstract fun storyRepository(impl: StoryRepositoryImpl): StoryRepository

    @Binds
    @Reusable
    abstract fun bookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Module
    internal object Providers {

        @Provides
        @Reusable
        fun storyDao(context: Context): StoryDao {
            return PersistenceComponent.factory()
                .create(context)
                .storyDao
        }

        @Provides
        @Reusable
        @FlowPreview
        @ExperimentalCoroutinesApi
        fun storyStore(
            newsApiService: NewsApiService,
            storyDao: StoryDao
        ): Store<Unit, List<Story>> {
            return StoreBuilder.fromNonFlow<Unit, List<StoryEntity>>(
                fetcher = {
                    newsApiService.headlines().map { it.toEntity() }
                }
            ).persister(
                reader = {
                    storyDao.allStories().map { stories ->
                        stories.map { it.toModel() }
                    }
                },
                writer = { _, stories ->
                    storyDao.updateStories(stories)
                },
                delete = {
                    storyDao.deleteAll()
                }
            ).build()
        }
    }
}