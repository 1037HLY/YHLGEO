fun loadAllTracks() {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            // 使用 collect 或 first() 从 Flow 中获取值
            val trackIds = database.trackPointDao().getAllTrackIds().first()
            val summaries = mutableListOf<TrackSummary>()
            for (id in trackIds) {
                val count = database.trackPointDao().getPointCount(id)
                val startTime = database.trackPointDao().getStartTimeMillis(id)
                val endTime = database.trackPointDao().getEndTimeMillis(id)
                if (startTime != null && count > 0) {
                    summaries.add(
                        TrackSummary(
                            trackId = id,
                            startTime = startTime,
                            endTime = endTime,
                            pointCount = count
                        )
                    )
                }
            }
            _uiState.value = _uiState.value.copy(
                trackList = summaries,
                isLoading = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
