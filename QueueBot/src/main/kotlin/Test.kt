suspend fun main(vararg args: String) {
//    val db = DB()
//    println("Start")
//


    val tableInfo = SpreadsheetInfo("1EmM8619VtPPd5svGF-vuXNVDf6vImsucU3GTXwUi9NE", "Лист1", "A1:D6")
    val table = Spreadsheet(tableInfo)
    val entries = table.getMapOfAllEntries()
    for ((k, v) in entries) {
        println("key \"$k\"")
        println(v)
    }
// Start a coroutine
//    fun getTb() = SheetsQuickstart.getQueues("1EmM8619VtPPd5svGF-vuXNVDf6vImsucU3GTXwUi9NE", "Лист1", "A1:D6")
//    var table = getTb()?.let { Table(it) }
//    GlobalScope.launch {
//        for (i in 0..30) {
//            delay(1000)
//            val tb2 = getTb()?.let { Table(it) }
//            if (table != null && tb2 != null) {
//                val ind = Table.compare(table!!, tb2)
//                if (ind != -1) {
//                    println("Change ${ind}th queue")
//                    table = tb2
//                }
////                println(table)
////                println(tb2)
//            }
////            println("Hello")
//        }
//    }
////    GlobalScope.launch {
////        for (i in 0..9) {
////            delay(1000)
////            println("Hello2")
////        }
////    }
//    Thread.sleep(2000) // wait for 2 seconds
//    println("Stop")
//
//    delay(1300L) // delay a bit
//    println("main: I'm tired of waiting!")
//    delay(30000L)
//    job.cancel() // cancels the job
//    job.join() // waits for job's completion
//    println("main: Now I can quit.")
}