package com.github.sikv.photos.di.module

import androidx.lifecycle.ViewModel
import com.github.sikv.photos.account.AccountManager
import com.github.sikv.photos.data.repository.FavoritesRepository
import com.github.sikv.photos.data.repository.PhotosRepository
import com.github.sikv.photos.recommendation.Recommender
import com.github.sikv.photos.viewmodel.MoreViewModel
import com.github.sikv.photos.viewmodel.SearchDashboardViewModel
import com.github.sikv.photos.viewmodel.SearchViewModel
import com.github.sikv.photos.viewmodel.ViewModelFactory
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Provider
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
class ViewModelModule {

    @Provides
    fun provideViewModelFactory(map: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>): ViewModelFactory {
        return ViewModelFactory(map)
    }

    @Provides
    @IntoMap
    @ViewModelKey(SearchDashboardViewModel::class)
    fun provideSearchDashboardViewModel(photosRepository: PhotosRepository, recommender: Recommender): ViewModel {
        return SearchDashboardViewModel(photosRepository, recommender)
    }

    @Provides
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    fun provideSearchViewModel(favoritesRepository: FavoritesRepository): ViewModel {
        return SearchViewModel(favoritesRepository)
    }

    @Provides
    @IntoMap
    @ViewModelKey(MoreViewModel::class)
    fun provideMoreViewModel(accountManager: AccountManager): ViewModel {
        return MoreViewModel(accountManager)
    }
}