package de.rogallab.mobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import de.rogallab.mobile.data.repositories.ImagesRepositoryImpl
import de.rogallab.mobile.data.repositories.PeopleRepositoryImpl
import de.rogallab.mobile.data.repositories.WordordersRepositoryImpl
import de.rogallab.mobile.domain.IPeopleRepository
import de.rogallab.mobile.domain.IPeopleUseCases
import de.rogallab.mobile.domain.IWorkorderUseCases
import de.rogallab.mobile.domain.IWorkordersRepository
import de.rogallab.mobile.domain.ImagesRepository
import de.rogallab.mobile.domain.usecases.people.PeopleUseCasesImpl
import de.rogallab.mobile.domain.usecases.workorders.WorkorderUseCasesImpl

@Module
@InstallIn(ViewModelComponent::class)
interface BindDataViewModelModules {
   @Binds
   @ViewModelScoped
   fun bindPeopleRepository(
      peopleRepositoryImpl: PeopleRepositoryImpl
   ): IPeopleRepository

   @Binds
   @ViewModelScoped
   fun bindWorkordersRepository(
      wordOrdersRepositoryImpl: WordordersRepositoryImpl
   ): IWorkordersRepository

   @Binds
   @ViewModelScoped
   fun bindImagesRepository(
      imagesRepositoryImpl: ImagesRepositoryImpl
   ): ImagesRepository
}