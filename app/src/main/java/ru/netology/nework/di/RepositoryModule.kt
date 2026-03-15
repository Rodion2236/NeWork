package ru.netology.nework.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.data.repository.AuthRepositoryImpl
import ru.netology.nework.data.repository.EventsRepositoryImpl
import ru.netology.nework.data.repository.JobsRepositoryImpl
import ru.netology.nework.data.repository.PostsRepositoryImpl
import ru.netology.nework.data.repository.UsersRepositoryImpl
import ru.netology.nework.domain.repository.AuthRepository
import ru.netology.nework.domain.repository.EventsRepository
import ru.netology.nework.domain.repository.JobsRepository
import ru.netology.nework.domain.repository.PostsRepository
import ru.netology.nework.domain.repository.UsersRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPostsRepository(impl: PostsRepositoryImpl): PostsRepository

    @Binds
    @Singleton
    abstract fun bindEventsRepository(impl: EventsRepositoryImpl): EventsRepository

    @Binds
    @Singleton
    abstract fun bindUsersRepository(impl: UsersRepositoryImpl): UsersRepository

    @Binds
    @Singleton
    abstract fun bindJobsRepository(impl: JobsRepositoryImpl): JobsRepository
}