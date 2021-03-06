class Table(data : MutableList<MutableList<Any>> ) {
    val queues : MutableMap<String, MutableList<String>>
    val teachers : MutableList<String>
    init {
        queues = mutableMapOf()
        teachers = mutableListOf()
        for (teacher in data[1]) {
            queues.put(teacher as String, mutableListOf())
            teachers.add(teacher)
            println(teacher)
        }
        for (rowIndex in 4..data.size) {
            val row = data[rowIndex]
            println(row)
            for (teacherColumn in 0 until queues.size) {
                val tchr = teachers[teacherColumn]
                if (row[teacherColumn].equals(null)) {
                    println("null")
                }
                queues[tchr]?.add(row[teacherColumn] as String)
            }
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

}