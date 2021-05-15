package cn.kter.ktutil

import android.util.SparseIntArray
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.util.getItemView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlin.properties.Delegates

/**
 * adapter dsl辅助类
 */
open class KQuickAdapter<T, VH : BaseViewHolder>(@LayoutRes private val layoutResId: Int = 0, dataList: MutableList<T>? = null, protected var _convert: ((VH, T) -> Unit)? = null) :
    BaseQuickAdapter<T, VH>(layoutResId, dataList) {
    //item整体的点击事件
    private var _itemClick: ((View, T) -> Unit)? = null
    var newDatas: MutableList<T>? = null
        set(value){
            value?.apply {
                use(value)
            }
        }
    override fun convert(holder: VH, item: T) {
        _convert?.invoke(holder, item)
        holder.itemView.setOnClickListener {
            _itemClick?.invoke(holder.itemView, item)
        }
    }


    fun convert(block: ((VH, T) -> Unit)) {
        _convert = block
        notifyDataSetChanged()
    }

    //item 点击事件
    fun itemClick(block: ((View, T) -> Unit)) {
        _itemClick = block
        notifyDataSetChanged()
    }

    //设置数据
    fun data(block: () -> MutableList<T>) {
        data = block()
        notifyDataSetChanged()
    }
    infix fun use(data:MutableList<T>){
        this.data=data
        notifyDataSetChanged()
    }
}


inline fun <reified T, reified VH : BaseViewHolder> RecyclerView.updateAdapter(block: KQuickAdapter<T, VH>.() -> Unit){
    block(this.adapter as KQuickAdapter<T, VH>)
}
/**
 * 多布局adapter
 */
class KMultiQuickAdapter<T : KMultiItem, VH : KBaseViewHolder>(data: MutableList<T>? = null) :
    KQuickAdapter<T, VH>(0, data) {

    fun Int.bindView(block: (holder: VH, item: T) -> Unit) {
        binds[this] = block
    }

    fun Int.createView(block: () -> Int) {
        layouts.put(this, block())
    }

    infix fun Int.to(block: Int.() -> Unit){
        apply(block)
    }

    private val layouts: SparseIntArray by lazy(LazyThreadSafetyMode.NONE) { SparseIntArray() }
    private val binds: MutableMap<Int, (holder: VH, item: T) -> Unit> by lazy { mutableMapOf<Int, (holder: VH, item: T) -> Unit>() }
    final override fun convert(holder: VH, item: T) {
        binds[getItemViewType(data.indexOf(item))]?.invoke(holder, item)
    }

    final override fun getDefItemViewType(position: Int): Int {
        return data[position].itemType
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutResId = layouts.get(viewType)
        require(layoutResId != 0) { "ViewType: $viewType found layoutResId，please use addItemType() first!" }
        return KBaseViewHolder(parent.getItemView(layoutResId)) as VH
    }

}

/**
 * dsl方法
 */

fun <T, VH : BaseViewHolder> RecyclerView.kQuickAdapter(@LayoutRes layoutResId: Int, dataList: MutableList<T>? = null, block: KQuickAdapter<T, VH>.() -> Unit) = KQuickAdapter<T, VH>(layoutResId, dataList).apply {
    block.invoke(this)
    this@kQuickAdapter.adapter = this
}

fun <T : KMultiItem, VH : KBaseViewHolder> RecyclerView.kMultiAdapter(dataList: MutableList<T>? = null, block: KMultiQuickAdapter<T, VH>.() -> Unit) = KMultiQuickAdapter<T, VH>(dataList).apply {
    block.invoke(this)
    this@kMultiAdapter.adapter = this
}

/**
 * 多布局类型
 */
interface KMultiItem {
    open var itemType: Int
}

/**
 * viewholder基类
 */
class KBaseViewHolder(view: View) : BaseViewHolder(view) {
//    protected val viewMap = mutableMapOf<Int, View>()
//
//    init {
//        scan(view)
//    }
//
//    /**
//     * 遍历View的方法
//     *
//     */
//    private fun scan(root: View) {
//        when (val v = root) {
//            is ViewGroup -> {
//                if (v.id != View.NO_ID) viewMap[v.id] = v
//                var childCount = v.childCount;
//                for (i in 0 until childCount) {
//                    scan(v.getChildAt(i));
//                }
//            }
//            else -> {
//                if (v.id != View.NO_ID) viewMap[v.id] = v
//            }
//        }
//    }

}
