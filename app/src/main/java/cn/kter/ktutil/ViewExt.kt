package cn.kter.ktutil

import android.view.View
import kotlinx.coroutines.flow.*

/**
 * view click事件转换为flow
 */
fun View.clickAsFlow(): Flow<Int> {
    val _countState = MutableStateFlow(0)
    this.setOnClickListener {
        _countState.value++
    }
    return _countState.drop(1)
}

