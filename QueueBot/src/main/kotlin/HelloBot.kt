import dev.inmo.micro_utils.coroutines.safely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.bot.Ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.asChannelChat
import dev.inmo.tgbotapi.extensions.utils.formatting.linkMarkdownV2
import dev.inmo.tgbotapi.extensions.utils.formatting.textMentionMarkdownV2
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingFlowsUpdatesByLongPolling
import dev.inmo.tgbotapi.types.ParseMode.MarkdownV2
import dev.inmo.tgbotapi.types.User
import dev.inmo.tgbotapi.types.chat.abstracts.*
import dev.inmo.tgbotapi.types.textField
import dev.inmo.tgbotapi.utils.extensions.escapeMarkdownV2Common
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.FileInputStream
import java.util.*

/**
 * The main purpose of this bot is just to answer "Oh, hi, " and add user mention here
 */
suspend fun main(vararg args: String) {
    val properties = Properties();
    properties.load(FileInputStream("build/resources/main/botInfo.properties"))

    val botToken = properties.getProperty("botInfo.token")
    val bot = telegramBot(botToken)
    println(bot.getMe())

    val scope = CoroutineScope(Dispatchers.Default)

    bot.buildBehaviour(scope) {
        println(getMe())

        onCommand("start") {
//            println(it.content.text)

            val chat = it.chat
            val name = when (chat) {
                is PrivateChat ->
                    chat.firstName + " " + chat.lastName
                else -> "who?"
            }
            reply(it, "Hi, $name:)")
        }
        onCommand("queue") {
            var text = ""
            println("!!!")
            val data = SheetsQuickstart.getQueues()
//            if (data != null) {
//                Table(data)
//            }
//            text += table.toString()
            println(data)
            reply(it, "Queue: \n$data")
        }
    }


//    bot.startGettingFlowsUpdatesByLongPolling(scope = scope) {
//        messageFlow.onEach {
//
//            safely {
//                val message = it.data
//                val chat = message.chat
////                println(message.text)
//                val answerText =  "Oh, hi, " + when (chat) {
//                    is PrivateChat -> "${chat.firstName} ${chat.lastName}".textMentionMarkdownV2(chat.id)
//                    is User -> "${chat.firstName} ${chat.lastName}".textMentionMarkdownV2(chat.id)
//                    is SupergroupChat -> (chat.username ?.username ?: bot.getChat(chat).inviteLink) ?.let {
//                        chat.title.linkMarkdownV2(it)
//                    } ?: chat.title
//                    is GroupChat -> bot.getChat(chat).inviteLink ?.let {
//                        chat.title.linkMarkdownV2(it)
//                    } ?: chat.title
//                    else -> "Unknown :(".escapeMarkdownV2Common()
//                }
//                bot.reply(
//                    message,
//                    answerText,
//                    MarkdownV2
//                )
//            }
//        }.launchIn(scope)
//        channelPostFlow.onEach {
//            safely {
//                val chat = it.data.chat
//                val message = "Hi everybody in this channel \"${(chat.asChannelChat()) ?.title}\""
//                bot.sendTextMessage(chat, message, MarkdownV2)
//            }
//        }.launchIn(scope)
//    }

    scope.coroutineContext[Job]!!.join()
}



