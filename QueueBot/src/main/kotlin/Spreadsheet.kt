class Spreadsheet {
    var table : MutableList<MutableList<String>> = mutableListOf()
    var columnsName : MutableList<String> = mutableListOf()
    constructor(columns : MutableList<String>, table : MutableList<MutableList<String>>) {
        this.table = table
        this.columnsName = columns
    }
    
    @SuppressWarnings("unchecked")
    constructor(data : MutableList<MutableList<Any>>?,
                columnShift : Int,
                tableShift : Int) {
        if (data != null) {
            val columns = data[columnShift]
            val table = data.subList(tableShift, data.size)
            this.table = table as MutableList<MutableList<String>>
            this.columnsName = columns as MutableList<String>
        }
    }

    constructor(data : MutableList<MutableList<Any>>) : this(data, 0, 1)
    constructor(info : SpreadsheetInfo,
                columnShift : Int,
                tableShift : Int) : this(info.getSpreadSheet(), columnShift, tableShift)
    constructor(info : SpreadsheetInfo) : this(info, 0, 1)

    companion object {
        fun findChangesInQueues(
            pairs1: List<Pair<Int, Int>>,
            pairs2: List<Pair<Int, Int>>
        ) : List<Int> {
            val list = mutableListOf<Int>()
            for (pair in pairs1) {
                val e = pairs2.firstOrNull {
                    it.first == pair.first &&
                    it.second < pair.second }
                if (e != null) {
                    list.add(e.first)
                }
            }
            val firsts = pairs1.map { it.first }
            val remains = pairs2.map { it.first }
                            .filter { !firsts.contains(it) }
            list.addAll(remains)
            return list
        }

        fun DMTable() : Spreadsheet? {
            val data = SheetsQuickstart.getQueues(
                SheetsQuickstart.DM_SPREAD_SHEETS_ID,
                SheetsQuickstart.DM_LIST_NAME,
                SheetsQuickstart.DM_RANGE
            )
            if (data != null) {
                data[0].add(0, "??????????")
            } else {
                return null
            }
            return Spreadsheet(data)
        }

        fun JAVATable() : Spreadsheet? {
            val data = SheetsQuickstart.getQueues(
                SheetsQuickstart.JAVA_SPREAD_SHEETS_ID,
                SheetsQuickstart.JAVA_LIST_NAME,
                SheetsQuickstart.JAVA_RANGE
            ) ?: return null
            return Spreadsheet(data, 1, 4)
        }

    }

    fun getQueue(index : Int) : MutableList<String>? {
        if (index < 0)
            return null
        val queue = mutableListOf<String>()
        for (row in table) {
            if (index < row.size)
                queue.add(row[index])
        }
        return queue
    }

    fun getQueues() : MutableList<MutableList<String>> {
        val queues = mutableListOf<MutableList<String>>()
        for (index in 0 until table.size) {
            queues.add(getQueue(index)!!)
        }
        return queues
    }

    fun getIndexOfQueue(columnName : String) : Int {
        return columnsName.indexOf(columnName)
    }

    fun getCell(column : Int, row : Int) : String? {
        val queue = getQueue(column)
        return if (queue == null
            || queue.isEmpty()
            || queue.size < row) {
            null
        } else {
            queue[row]
        }
    }

    fun getEntriesOf(element : String) : List<Pair<Int, Int>> {
        val entries = mutableListOf<Pair<Int, Int>>()
        var columnIndex = 0
        for (queue in getQueues()) {
            val elementIndex = queue.indexOf(element)
            if (elementIndex != -1) {
                entries.add(Pair(columnIndex, elementIndex))
            }
            columnIndex++
        }
        return entries
    }

    fun getMapOfAllEntries() : Map<String, List<Pair<Int, Int>>> {
        val map = mutableMapOf<String, MutableList<Pair<Int, Int>>>()
        var column = 0;
        for (queue in getQueues()) {
            var cellIndex = 0
            for (cell in queue) {
                if (cell.isNotEmpty()) {
                    map.getOrPut(cell) { mutableListOf() }
                        .add(Pair(column, cellIndex))
                }
                cellIndex++
            }
            column++
        }
        return map
    }

    fun findPositionOf(element : String, column : Int) : Int {
        val cnts = getQueue(column)!!.count { it.equals(element) }
        if (cnts > 1 || cnts == 0) {
            return -1
        }
        return getQueue(column)!!.indexOf(element)
    }

    fun findDifferencesFrom(table : Spreadsheet) : List<Pair<String, Pair<Int, Int>>> {
        val result = mutableListOf<Pair<String, Pair<Int, Int>>>()
        var columnIndex = 0
        for (queue in getQueues()) {
            var position = 0
            for (cell in queue) {
                val index = table.findPositionOf(cell, columnIndex)
                if (index != -1 && index < position) {
                    result.add(Pair(cell, Pair(columnIndex, index)))
                }
                position++
            }
            columnIndex++
        }
        return result
    }


    fun isValidRow(row : Int) : Boolean {
        return row >= 0 && row < table.size
    }
}

data class SpreadsheetInfo(
    val id : String,
    val listName : String,
    val range : String, ) {
    
    fun getSpreadSheet() : MutableList<MutableList<Any>>? {
        return SheetsQuickstart.getQueues(id, listName, range)
    }
    
}

