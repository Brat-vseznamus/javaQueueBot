import dev.inmo.tgbotapi.utils.extensions.escapeMarkdownV2Common

class Table(data : MutableList<MutableList<Any>>, shift1: Int = 0, shift2: Int = 1) {
    private val queues : MutableMap<String, MutableList<String>> = mutableMapOf()
    private val teachers : MutableList<String> = mutableListOf()

    init {
        for (teacher in data[shift1]) { // 1 и 4
            queues[teacher as String] = mutableListOf()
            teachers.add(teacher)
        }
        for (rowIndex in shift2 until data.size) {
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

    companion object {
        fun DMTable() : Table {
            val data = SheetsQuickstart.getQueues(
                SheetsQuickstart.DM_SPREAD_SHEETS_ID,
                SheetsQuickstart.DM_LIST_NAME,
                SheetsQuickstart.DM_RANGE
            )
            if (data != null) {
                data[0].add(0, "Имена")
            }
            return Table(data!!)
        }

        fun JAVATable() : Table? {
            val data = SheetsQuickstart.getQueues(
                SheetsQuickstart.JAVA_SPREAD_SHEETS_ID,
                SheetsQuickstart.JAVA_LIST_NAME,
                SheetsQuickstart.JAVA_RANGE
            ) ?: return null
            return Table(data, 1, 4)
        }

        fun compare(tb1 : Table, tb2 : Table) : Int {
            var changedQueue = 0
            if (tb1.teachers.size != tb2.teachers.size)
                return -1
            for (teacher in tb1.teachers) {
                if (tb1.queues[teacher]!!.size != tb2.queues[teacher]!!.size) {
                    return changedQueue
                }
                for (studentInd in 0 until tb1.queues[teacher]!!.size) {
                    val st1 = tb1.queues[teacher]!![studentInd]
                    val st2 = tb2.queues[teacher]!![studentInd]
                    if (st1 != st2) {
                        return changedQueue
                    }
                }
                changedQueue++
            }
            return -1
        }

    }

    override fun toString() : String {
        var str = ""
        for ((key, value) in queues) {
            str += key
            str += value
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