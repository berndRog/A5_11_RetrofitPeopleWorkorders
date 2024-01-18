package de.rogallab.mobile.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import de.rogallab.mobile.domain.IPeopleUseCases
import de.rogallab.mobile.domain.IWorkorderUseCases
import de.rogallab.mobile.domain.usecases.people.PeopleUseCasesImpl
import de.rogallab.mobile.domain.usecases.workorders.WorkorderUseCasesImpl

@Module
@InstallIn(ViewModelComponent::class)
interface BindDomainModules {

   @Binds
   @ViewModelScoped
   fun bindPeopleUseCases(
      peopleRepositoryImpl: PeopleUseCasesImpl
   ): IPeopleUseCases

   @Binds
   @ViewModelScoped
   fun bindWorkorderUseCases(
      workOrderUseCasesImpl: WorkorderUseCasesImpl
   ): IWorkorderUseCases

}