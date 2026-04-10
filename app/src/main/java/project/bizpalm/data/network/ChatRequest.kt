package project.bizpalm.data.network

data class ChatRequest(
    val model: String = "llama3.1",
    val messages: List<Message>,
    val tools: List<Tool>? = null,
    val tool_choice: String? = "auto"
)

data class Message(
    val role: String,
    val content: String? = null,
    val tool_calls: List<ToolCall>? = null,
    val tool_call_id: String? = null
)

data class Tool(
    val type: String = "function",
    val function: FunctionDef
)

data class FunctionDef(
    val name: String,
    val description: String,
    val parameters: Parameters
)

data class Parameters(
    val type: String = "object",
    val properties: Map<String, Property>,
    val required: List<String>
)

data class Property(
    val type: String,
    val description: String
)

data class ToolCall(
    val id: String,
    val type: String,
    val function: ToolFunction
)

data class ToolFunction(
    val name: String,
    val arguments: String
)
