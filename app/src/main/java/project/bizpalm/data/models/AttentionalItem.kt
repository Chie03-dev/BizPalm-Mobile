package project.bizpalm.data.models

import project.bizpalm.data.entities.Product

data class AttentionalItem(
    val product: Product,
    val category: String, // e.g., "Critical Stock", "Profit Driver", "Bundle Deal", "Slow Mover"
    val reason: String,   // Detailed explanation of why it's here
    val actionSuggestion: String // What the owner should do
)
