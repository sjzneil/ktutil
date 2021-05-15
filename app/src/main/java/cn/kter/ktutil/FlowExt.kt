package cn.kter.ktutil

import android.os.Looper
import android.util.Log
import androidx.annotation.CheckResult
import androidx.annotation.RestrictTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

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

@CheckResult
@OptIn(ExperimentalCoroutinesApi::class)
fun Lifecycle.events(): Flow<Lifecycle.Event> = callbackFlow {
    checkMainThread()
    val observer = object : LifecycleObserver {
        @Suppress("UNUSED_PARAMETER")
        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        fun onEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
            safeOffer(event)
        }
    }
    addObserver(observer)
    awaitClose {
        Log.d("life", "close")
        removeObserver(observer)
    }
}
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@OptIn(ExperimentalCoroutinesApi::class)
fun <E> SendChannel<E>.safeOffer(value: E): Boolean {
    return runCatching { offer(value) }.getOrDefault(false)
}
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun checkMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "Expected to be called on the main thread but was " + Thread.currentThread().name
    }
}
val EMPTY_ZIP: suspend (Any, Any) -> Unit= { _: Any, _: Any -> }

