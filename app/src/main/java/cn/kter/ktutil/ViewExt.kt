package cn.kter.ktutil

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*




fun TextView.changeAsFlow():Flow<String> = callbackFlow{
    checkMainThread()
    val listener = object:TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            safeOffer((s?:"").toString())
        }

        override fun afterTextChanged(s: Editable?) {
        }

    }
    addTextChangedListener(listener)
    awaitClose {
        removeTextChangedListener(listener)
    }
}
fun CompoundButton.isCheckedAsFlow():Flow<Boolean> = callbackFlow {
    checkMainThread()
    val listener = CompoundButton.OnCheckedChangeListener { _, isChecked -> safeOffer(isChecked)}
    setOnCheckedChangeListener(listener)
    awaitClose {
        setOnCheckedChangeListener(null)
    }
}

fun RadioGroup.selectedAsFlow():Flow<Int> = callbackFlow {
    checkMainThread()
    val listener = RadioGroup.OnCheckedChangeListener{_, id->safeOffer(id)}
    setOnCheckedChangeListener(listener)
    awaitClose {
        setOnCheckedChangeListener(null)
    }
}



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

interface Action
object NOSTATE:Action

val RecyclerView.action: MutableLiveData<Action> by lazy {
    MutableLiveData(NOSTATE)
}

fun RecyclerView.itemClickAsflow():Flow<Action> = callbackFlow {
    val observer:Observer<Action> = Observer<Action> { t -> safeOffer(t) }
    action.observeForever(observer)
    awaitClose { action.removeObserver(observer)}
}.conflate().drop(1)

@CheckResult
@OptIn(ExperimentalCoroutinesApi::class)
fun RecyclerView.scrollAsFlow(): Flow<RecyclerViewScrollAction> = callbackFlow<RecyclerViewScrollAction> {
    checkMainThread()
    val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            safeOffer(
                RecyclerViewScrollAction(
                    dx = dx,
                    dy = dy
                )
            )
        }
    }
    addOnScrollListener(listener)
    awaitClose {
        removeOnScrollListener(listener)
    }
}.conflate()

data class RecyclerViewScrollAction(
    public val dx: Int,
    public val dy: Int
):Action

