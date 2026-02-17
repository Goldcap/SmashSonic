package com.smashsonic.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashsonic.data.model.BackgroundType
import com.smashsonic.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackgroundViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val backgroundType: StateFlow<BackgroundType> = settingsRepository.backgroundType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BackgroundType.SOLID_CYAN)

    fun setBackgroundType(type: BackgroundType) {
        viewModelScope.launch {
            settingsRepository.setBackgroundType(type)
        }
    }
}
