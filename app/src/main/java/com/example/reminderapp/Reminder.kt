data class Reminder(
    val title: String,
    val time: String,
    val status: String,
    val description: String,
    val date: String,
    val hasAlarm: Boolean = false,
    var isSelected: Boolean = false
)
