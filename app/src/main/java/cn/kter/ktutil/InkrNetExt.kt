package cn.kter.ktutil


import android.util.MalformedJsonException
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException
import java.lang.reflect.Type
import java.net.URISyntaxException

/**
 * author : ning
 * company：inkr
 * desc   :
 */
class NetStatus(var error: Exception?, var willDone: Boolean = false)
typealias Executable<T> = suspend () -> T
typealias ReturnBlock<T> = suspend (T) -> Unit


/**
 * 同步处理网络请求流程和ui显示流程
 */
suspend infix fun <T> Executable<T>.execute( backBlock: suspend (T) -> Unit): NetStatus {
    val data: T
    try {
        data = this.invoke()
    } catch (e: Exception) {
        e.printStackTrace()
        return NetStatus(e)
    }
    backBlock(data)
    return NetStatus(null)
}


/**
 * 筛选异常类型
 */
infix fun NetStatus.filter(block: () -> Array<Class<*>>): NetStatus {
    val clazzes = block()
    clazzes.forEach {
        this.error?.javaClass?.apply {
            if (it.isAssignableFrom(error?.javaClass).orFalse()) {
                return NetStatus(error, true)
            }
        }
    }
    return this
}

/**
 * 处理
 */
suspend infix fun NetStatus.then(block: suspend () -> Unit): NetStatus {
    if (willDone) {
        error?.apply {
            block()
        }
    }
    return NetStatus(error, false)
}
/**
 * 如果为 null 则为 false
 */
fun Boolean?.orFalse(): Boolean {
    return this ?: false
}