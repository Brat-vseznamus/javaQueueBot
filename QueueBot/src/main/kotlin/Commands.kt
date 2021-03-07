import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.ParseMode.Markdown
import dev.inmo.tgbotapi.types.chat.abstracts.Chat
import dev.inmo.tgbotapi.types.chat.abstracts.PrivateChat
import kotlinx.coroutines.Job

fun getTagName(chat : Chat) : String {
    val name = when (chat) {
        is PrivateChat ->
            chat.firstName + " " + chat.lastName
        else -> "who?"
    }
    return name
}

fun log(str : String, chat : Chat) {
    println("${getTagName(chat)}: $str")
}

public suspend inline fun BehaviourContext.startCommand() : Job =
    onCommand("start") {
        val chat = it.chat
        val name = when (chat) {
            is PrivateChat ->
                chat.firstName + " " + chat.lastName
            else -> "who?"
        }
        reply(it, "Hi, $name:)")
    }


public suspend inline fun BehaviourContext.queueCommand() : Job =
    onCommand("queue", false) {
        val messageText = it.content.text
        val addInfo = messageText.substringAfter("/queue")
            .filter { !it.isWhitespace() }
        val index = getTeacher(addInfo)
        log("check queue", it.chat)
        val tb = Table.JAVATable()
        if (tb != null) {
            var text = ""
            if (index == -1) {
                text += if (addInfo.isEmpty())
                    "" else
                    "Teacher \"$addInfo\" not found.\n"
                text += tb.toMarkDownString()
            } else {
                text += tb.getQueue(getTeacherName(index))
            }
            sendTextMessage(it.chat, text, Markdown)
        }
    }

//val tmpUserMap = mutableMapOf<String, String>(
//    "@Quicksmart" to "Будущев Матвей Ярославович")

fun getName(usertag : String) : String? {
    return db.getUserName(usertag)
}

public suspend inline fun BehaviourContext.findMeCommand() : Job =
    onCommand("findme") {
        val chat = it.chat
        val usertag = when (chat) {
            is PrivateChat ->
                chat.username?.username
            else -> ""
        }
        log("search himself in queue", it.chat)
        if (usertag != null) {
            var name = getName(usertag)
            if (name == null)
                name = ""
            val tb = Table.JAVATable()
            if (tb != null) {
                sendTextMessage(it.chat, tb.find(name), Markdown)
            }
        }
    }

fun setName(usertag : String, username : String) {
    db.addUser(usertag, username)
}

public suspend inline fun BehaviourContext.setNameCommand() : Job =
    onCommand("setname", false) {
        val messageText = it.content.text
        val addInfo = messageText.substringAfter("/setname ")
        val dm = Table.DMTable()
        val chat = it.chat
        val usertag = when (chat) {
            is PrivateChat ->
                chat.username?.username
            else -> ""
        }
        log("try to set name to $addInfo", it.chat)
        if (dm.checkExisting(addInfo)) {
            if (usertag != null) {
                setName(usertag, addInfo)
            }
            sendTextMessage(it.chat, "Your name changed successfully to $addInfo!", Markdown)
        } else {
            sendTextMessage(it.chat, "Unknown student: $addInfo", Markdown)
        }
    }