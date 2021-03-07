import dev.inmo.tgbotapi.utils.extensions.escapeMarkdownV2Common

class Table(data : MutableList<MutableList<Any>> ) {
    private val queues : MutableMap<String, MutableList<String>> = mutableMapOf()
    private val teachers : MutableList<String> = mutableListOf()

    init {
        for (teacher in data[1]) {
            queues.put(teacher as String, mutableListOf())
            teachers.add(teacher)
        }
        for (rowIndex in 4 until data.size) {
            val row = data[rowIndex]
            for (teacherColumn in 0 until teachers.size) {
                if (teacherColumn < row.size) {
                    val tchr = teachers[teacherColumn]
                    val cell = row[teacherColumn]
                    if (cell.hashCode() != 0) {
                        queues[tchr]?.add(row[teacherColumn] as String)
                    }
                } else {
                    break
                }
            }
        }
    }
    override fun toString() : String {
        var str = ""
        for ((key, value) in queues) {
            str += "$key: "
            str += value
            str += '\n'
        }
        return str
    }

    fun toMarkDownString() : String {
        var str = ""
        for ((key, _) in queues) {
            str += getQueue(key)
        }
        return str
    }

    fun getQueue(name : String) : String {
        var str = "*$name*: \n"
        var index = 1;
        for (student in queues[name]!!)
            str += " #${index++} _$student\n_"
        str += '\n'
        return str
    }

    fun find(user : String) : String {
        var str = ""
        for (queue in queues) {
            val index = queue.value.indexOf(user)
            if (index != -1) {
                str+= "You're *${1 + index}"
                str+= when (1 + index) {
                    1 -> "st "
                    2 -> "nd "
                    3 -> "rd "
                    else -> "th "
                }
                str+= "*in order to *${queue.key}*\n"
                break;
            }
        }
        if (str.isEmpty()) {
            str += "You're not found in any queue"
        }
        return str
    }

}