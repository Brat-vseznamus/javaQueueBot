import dev.inmo.micro_utils.coroutines.safely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.bot.Ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.asChannelChat
import dev.inmo.tgbotapi.extensions.utils.formatting.linkMarkdownV2
import dev.inmo.tgbotapi.extensions.utils.formatting.textMentionMarkdownV2
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingFlowsUpdatesByLongPolling
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.ChatIdentifier
import dev.inmo.tgbotapi.types.ParseMode.Markdown
import dev.inmo.tgbotapi.types.ParseMode.MarkdownV2
import dev.inmo.tgbotapi.types.User
import dev.inmo.tgbotapi.types.chat.abstracts.*
import dev.inmo.tgbotapi.types.textField
import dev.inmo.tgbotapi.utils.extensions.escapeMarkdownV2Common
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*

/**
 * The main purpose of this bot is just to answer "Oh, hi, " and add user mention here
 */

internal val db : DB = DB()
fun sendMessage(chatId : Int, text : String, botToken : String) {
    var urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s"
    urlString = String.format(urlString, botToken, chatId.toString(), text)
    val conn = URL(urlString).openConnection()
    val inputStream = BufferedInputStream(conn.getInputStream())
    val br = BufferedReader(InputStreamReader(inputStream))
    val response = br.readText()
}

//fun sendRequestToStart(chatId : Int, text : String, botToken : String) {
//    var urlString = "https://api.telegram.org/bot%s/start"
//    urlString = String.format(urlString, botToken)
//    val conn = URL(urlString).openConnection()
//    val inputStream = BufferedInputStream(conn.getInputStream())
//    val br = BufferedReader(InputStreamReader(inputStream))
//    val response = br.readText()
//}

suspend fun main(vararg args: String) {
    val botproperties = Properties();
    botproperties.load(FileInputStream("build/resources/main/botInfo.properties"))
    val botToken = botproperties.getProperty("botInfo.token")
    val bot = telegramBot(botToken)
    println(bot.getMe())
    println(db.getUsers())
    val scope = CoroutineScope(Dispatchers.Default)
    //var javatb = Table.JAVATable()
    fun getTb() = SheetsQuickstart.getQueues("1EmM8619VtPPd5svGF-vuXNVDf6vImsucU3GTXwUi9NE", "Лист1", "A1:D6")
    var table = getTb()?.let { Table(it) }

    bot.buildBehaviour(scope) {
        startCommand()
        queueCommand()
        findMeCommand()
        setNameCommand()
    }


    GlobalScope.launch {
        while (true) {
            delay(2000L)
            val newtb = getTb()?.let { Table(it) }
            if (table != null && newtb != null) {
                val ind = Table.compare(table!!, newtb)
                if (ind != -1) {
                    val queue = newtb.getQueue(ind)
                    if (queue != null && queue.isNotEmpty()) {
                        if (queue[0] != table!!.getQueue(ind)!![0]) {
                            val newHeadName = queue[0]
                            if (db.containsUserName(newHeadName)) {
                                sendMessage(db.getChatIdByName(newHeadName),
                                        "${db.getUserTag(newHeadName)},%0A" +
                                                "You're next to ${getTeacherName(ind)}",
                                            botToken)
                            }
                            table = newtb
                        }
                    }
                }
            }
        }
    }
    scope.coroutineContext[Job]!!.join()
}





