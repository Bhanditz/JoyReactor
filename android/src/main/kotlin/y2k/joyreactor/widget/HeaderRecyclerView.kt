package y2k.joyreactor.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import y2k.joyreactor.R
import y2k.joyreactor.common.ViewResolver
import y2k.joyreactor.common.findOrNull
import y2k.joyreactor.common.inflate
import y2k.joyreactor.common.use

/**
 * Created by y2k on 2/28/16.
 */
class HeaderRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs), ViewResolver {

    // TODO: убрать лишние поле
    private var headerLayout = 0
    private val holder by lazy { HeaderViewHolder(inflate(headerLayout)) }

    init {
        context.theme
            .obtainStyledAttributes(attrs, R.styleable.HeaderRecyclerView, 0, 0)
            .use {
                headerLayout = getResourceId(R.styleable.HeaderRecyclerView_header, 0)
            }
    }

    override fun <T> find(id: Int): T? {
        return holder.itemView.findOrNull<T>(id)
    }

    @Suppress("UNCHECKED_CAST")
    override fun setAdapter(adapter: Adapter<*>) {
        super.setAdapter(HeaderAdapter(adapter as Adapter<ViewHolder>))
    }

    inner class HeaderAdapter(
        private val base: RecyclerView.Adapter<ViewHolder>) : RecyclerView.Adapter<ViewHolder>() {

        init {
            setHasStableIds(base.hasStableIds())
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) 111 else base.getItemViewType(position - 1)
        }

        override fun getItemId(position: Int): Long {
            return if (position == 0) 111L else base.getItemId(position - 1)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position > 0) base.onBindViewHolder(holder, position - 1)
        }

        override fun getItemCount(): Int {
            return 1 + base.itemCount
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return if (viewType == 111) holder else base.onCreateViewHolder(parent, viewType)
        }

        override fun registerAdapterDataObserver(observer: AdapterDataObserver?) {
            base.registerAdapterDataObserver(observer)
        }

        override fun unregisterAdapterDataObserver(observer: AdapterDataObserver?) {
            base.unregisterAdapterDataObserver(observer)
        }
    }

    private class HeaderViewHolder(itemView: View?) : ViewHolder(itemView)
}