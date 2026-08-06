package com.geosurvey.toolbox.domain.usecase

import com.geosurvey.toolbox.data.repository.LocationRepository
import com.geosurvey.toolbox.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

class GetLocationUseCase(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<LocationPoint> = repository.getLocationFlow()
}
