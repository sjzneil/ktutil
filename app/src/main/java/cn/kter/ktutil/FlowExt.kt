package cn.kter.ktutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

/**
 * 防止频繁产生事件
 */
fun <T> Flow<T>.threshold(duration: Long): Flow<T> {
    var last=0L
    return transform { value->
        val now= System.currentTimeMillis()
        if(now-last>duration){
            emit(value)
            last=now
        }
    }
}