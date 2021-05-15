package cn.kter.ktutil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class MainActivity : AppCompatActivity(), V {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rv=this@MainActivity.findViewById<RecyclerView>(R.id.rv)
        findViewById<RecyclerView>(R.id.rv).layoutManager=LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.rv).kQuickAdapter<Int, BaseViewHolder>(R.layout.item){
            data {
                (1..100).toMutableList()
            }
            convert { baseViewHolder, i ->
                baseViewHolder.getView<View>(R.id.ibtn).setOnClickListener {
                    val s=RVItemClickAction(it.hashCode(), i)
                    rv.action.value=(s)
                }
            }
        }

        P().bindIntents(this)
    }
    override fun clickIntent() = kotlin.run {
        var count=0
        findViewById<Button>(R.id.btn)
            .clickAsFlow().transform {
                emit(count++)
            }
    }

    override fun click2Intent(): Flow<Int> = findViewById<Button>(R.id.btn2).clickAsFlow()
    override fun rvItemClick(): Flow<Action> = findViewById<RecyclerView>(R.id.rv).itemClickAsflow()
    override fun render(state: State) {
        when(state){
            is TestState->{
                Toast.makeText(this, "click ${state.v}", Toast.LENGTH_LONG).show()
            }
            Error->{
                Toast.makeText(this, "${state.toString()}", Toast.LENGTH_LONG).show()
            }
        }
    }
}


data class RVItemClickAction(val v:Int, val count:Int):Action

sealed class MainState: State

data class TestState(val v:Int):MainState()

object Error:MainState()

interface V:MviView<State>{
    fun clickIntent():Flow<Int>
    fun click2Intent():Flow<Int>
    fun rvItemClick(): Flow<Action>
}
class P:MviPresent<State, MainActivity>{
    override fun bindIntents(v: MainActivity) {
        v.clickIntent().threshold(500).zip(
            v.click2Intent().threshold(500), EMPTY_ZIP
        ).transform {
            suspend {
                delay(3000)
                9999
            } execute {
                emit(TestState(it))
            } filter {
                arrayOf(Exception::class.java)
            } then {
                emit(Error)
            }
        }.onEach {
            v.render(it)
        }.launchIn(v.lifecycleScope)

        v.rvItemClick().onEach {
            when(it){
                is RVItemClickAction->{
                    v.render(TestState(it.count))
                }
            }
        }.launchIn(v.lifecycleScope)
    }
}