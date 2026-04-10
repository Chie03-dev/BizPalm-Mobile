package project.bizpalm.ui.analytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import project.bizpalm.R
import project.bizpalm.data.models.AttentionalItem

class AttentionalItemAdapter : RecyclerView.Adapter<AttentionalItemAdapter.ViewHolder>() {

    private var items: List<AttentionalItem> = emptyList()
    private var onItemClickListener: ((AttentionalItem) -> Unit)? = null

    fun setItems(newItems: List<AttentionalItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (AttentionalItem) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attentional_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attentionalItem = items[position]
        holder.bind(attentionalItem)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryTag: TextView = itemView.findViewById(R.id.tvCategoryTag)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvReason: TextView = itemView.findViewById(R.id.tvReason)
        private val tvActionSuggestion: TextView = itemView.findViewById(R.id.tvActionSuggestion)
        private val ivProductIcon: ImageView = itemView.findViewById(R.id.ivProductIcon)

        fun bind(attentionalItem: AttentionalItem) {
            tvProductName.text = attentionalItem.product.productName
            tvReason.text = attentionalItem.reason
            tvActionSuggestion.text = attentionalItem.actionSuggestion
            tvCategoryTag.text = attentionalItem.category.uppercase()

            // Dynamic Styling based on category to fix all-red issue
            when (attentionalItem.category) {
                "Critical Stock" -> {
                    tvCategoryTag.setBackgroundResource(R.drawable.bg_tag_red)
                    ivProductIcon.setImageResource(R.drawable.ic_cube) 
                }
                "Profit Driver" -> {
                    tvCategoryTag.setBackgroundResource(R.drawable.bg_tag_blue)
                    ivProductIcon.setImageResource(R.drawable.ic_save)
                }
                "Bundle Opportunity" -> {
                    tvCategoryTag.setBackgroundResource(R.drawable.bg_tag_green)
                    ivProductIcon.setImageResource(R.drawable.ic_save)
                }
                "Slow Mover" -> {
                    tvCategoryTag.setBackgroundResource(R.drawable.bg_tag_orange)
                    ivProductIcon.setImageResource(R.drawable.ic_calendar)
                }
                else -> {
                    tvCategoryTag.setBackgroundResource(R.drawable.bg_tag_blue)
                    ivProductIcon.setImageResource(R.drawable.ic_cube)
                }
            }

            itemView.setOnClickListener { onItemClickListener?.invoke(attentionalItem) }
        }
    }
}
