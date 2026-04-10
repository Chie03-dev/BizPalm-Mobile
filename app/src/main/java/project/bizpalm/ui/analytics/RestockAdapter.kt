package project.bizpalm.ui.analytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import project.bizpalm.R
import project.bizpalm.data.models.RestockRecommendation
import java.util.Locale

class RestockAdapter(private var recommendations: List<RestockRecommendation> = emptyList()) :
    RecyclerView.Adapter<RestockAdapter.RestockViewHolder>() {

    private var listener: ((RestockRecommendation) -> Unit)? = null

    fun setOnItemClickListener(listener: (RestockRecommendation) -> Unit) {
        this.listener = listener
    }

    fun updateData(newRecommendations: List<RestockRecommendation>) {
        this.recommendations = newRecommendations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restock_recommendation, parent, false)
        return RestockViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestockViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }

    override fun getItemCount(): Int = recommendations.size

    inner class RestockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvUrgency: TextView = itemView.findViewById(R.id.tvUrgency)
        private val tvQty: TextView = itemView.findViewById(R.id.tvSuggestedQty)

        fun bind(recommendation: RestockRecommendation) {
            tvName.text = recommendation.product.productName
            
            val urgencyText = when {
                recommendation.daysLeft <= 0 -> "Out of stock / Critical"
                recommendation.daysLeft == 1 -> "Runs out in 1 day"
                else -> "Runs out in ${recommendation.daysLeft} days"
            }
            tvUrgency.text = urgencyText
            
            // Set color based on urgency
            tvUrgency.setTextColor(if (recommendation.daysLeft <= 3) 
                android.graphics.Color.parseColor("#E53935") // Red
            else 
                android.graphics.Color.parseColor("#FB8C00") // Orange
            )

            tvQty.text = String.format(Locale.getDefault(), "+%d units", recommendation.suggestedRestock)

            itemView.setOnClickListener {
                listener?.invoke(recommendation)
            }
        }
    }
}
