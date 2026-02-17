package com.smashsonic.ui.home

import androidx.lifecycle.ViewModel
import com.smashsonic.data.remote.SubsonicUrlBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeScreenUrlHelper @Inject constructor(
    val urlBuilder: SubsonicUrlBuilder,
) : ViewModel()
