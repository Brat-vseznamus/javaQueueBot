import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
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
            chat.firstName + " " + chat.lastName +"(${chat.id.chatId})"
        else -> "who?"
    }
    return name
}

fun log(str : String, chat : Chat) {
    println("${getTagName(chat)}: $str")
}

suspend inline fun BehaviourContext.startCommand() : Job =
    onCommand("start") {
        val chat = it.chat
        val name = when (chat) {
            is PrivateChat ->
                chat.firstName + " " + chat.lastName
            else -> "who?"
        }
        reply(it, "Hi, $name:)")
    }


suspend inline fun BehaviourContext.queueCommand() : Job =
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

fun getName(usertag : String) : String? {
    return db.getUserName(usertag)
}

suspend inline fun BehaviourContext.findMeCommand() : Job =
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

fun setName(usertag : String, username : String, userChatId : Int) {
    db.addUser(usertag, username, userChatId)
}


suspend inline fun BehaviourContext.setNameCommand() : Job =
    onCommand("setname", false) {
        val messageText = it.content.text
        var addInfo = messageText.substringAfter("/setname")
        if (addInfo.isNotEmpty()) {
            addInfo = addInfo.substringAfter(' ')
        } else {
            sendTextMessage(it.chat, "Type something after command", Markdown)
            return@onCommand
        }
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
                val chatId = when(chat) {
                    is PrivateChat -> chat.id.chatId.toLong()
                    else -> 0
                }
                setName(usertag, addInfo, chatId.toInt())
            }
            sendTextMessage(it.chat, "Your name changed successfully to $addInfo!", Markdown)
        } else {
            sendTextMessage(it.chat, "Unknown student: $addInfo", Markdown)
        }
    }

fun updateMuteStatus(usertag: String, mutestatus : Boolean) {
    db.updateMuteStatus(usertag, mutestatus)
}

suspend inline fun BehaviourContext.muteCommand(): Job =
    onCommand("mute", false) {
        val strAfter = it.content.text.substringAfter("/mute")
        var mutestatus = true
        if (strAfter.isNotEmpty()) {
            val intValue = strAfter.filter { !it.isWhitespace() }
                                    .toIntOrNull()
            if (intValue != null && intValue == 0) {
                mutestatus = false
            }
        }
        val chat = it.chat
        val usertag = when (chat) {
            is PrivateChat ->
                chat.username?.username
            else -> ""
        }
        sendTextMessage(
            it.chat,
            "Now notifications are *${if (mutestatus) "" else "un"}muted*",
            Markdown)
        updateMuteStatus(usertag!!, mutestatus)
    }

suspend inline fun BehaviourContext.helpCommand() : Job =
    onCommand("help") {
        val ch = '+'
        val text =
            "*queue* - показать все очереди\n" +
            "$ch ``queue`` - все очереди\n" +
            "$ch ``queue <фамилия преподавателя>`` - конкретная очередь\n\n" +
            "*findme* - найти себя в очереди\n\n" +
            "*setname* - установить имя\n" +
            "$ch ``setname <ФИО>`` - полное ФИО, как в табличке по ДМ\n\n" +
            "*mute* - включить/отключить оповещения\n" +
            "$ch ``mute`` - отключить оповещения\n" +
            "$ch ``mute 1`` - отключить оповещения\n" +
            "$ch ``mute 0`` - включить оповещения\n"
        sendTextMessage(it.chat, text, Markdown)
    }