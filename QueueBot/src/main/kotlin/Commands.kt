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
        else -> "stas"
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
        log("check queue $addInfo", it.chat)
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
        val chat = it.chat
        val usertag = when (chat) {
            is PrivateChat ->
                chat.username?.username
            else -> ""
        }
        val dm = Table.DMTable()
        log("try to set name to $addInfo", it.chat)
        if (dm.checkExisting(addInfo) || Table.DMTable(1)!!.checkExisting(addInfo)) {
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
        log("try to ${if (mutestatus) "" else "un"}mute", it.chat)
        sendTextMessage(
            it.chat,
            "Now notifications are *${if (mutestatus) "" else "un"}muted*",
            Markdown)
        updateMuteStatus(usertag!!, mutestatus)
    }

fun setTime(usertag : String, time : Int) {
    db.addTime(usertag, time)
}

suspend inline fun BehaviourContext.setNotificationCommand(): Job =
    onCommand("setnotification", false) {
        val strAfter = it.content.text.substringAfter("/setnotification")
        var position = 0
        if (strAfter.isNotEmpty()) {
            val intValue = strAfter.filter { !it.isWhitespace() }
                .toIntOrNull()
            if (intValue != null && intValue >= 1) {
                position = intValue
            } else {
                sendTextMessage(
                    it.chat,
                    "Wrong input: \"$strAfter\"",
                    Markdown)
                return@onCommand
            }
        } else {
            sendTextMessage(
                it.chat,
                "Wrong input: \"$strAfter\"",
                Markdown)
            return@onCommand
        }
        sendTextMessage(
            it.chat,
            "Notification set on ${getNumeral(position)} position",
            Markdown)
        val chat = it.chat
        log("set additional notification on $position", chat)
        val usertag = when (chat) {
            is PrivateChat ->
                chat.username?.username
            else -> ""
        }
        setTime(usertag!!, position)
    }

suspend inline fun BehaviourContext.helpCommand() : Job =
    onCommand("help") {
        val ch = "  "
        log("need to help", it.chat)
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
            "$ch ``mute 0`` - включить оповещения\n\n" +
            "*setnotification* - установить уведомление на более раннюю позицию, чем 1\n" +
            "$ch ``setnotification <число>`` - число >= 2\n"
        sendTextMessage(it.chat, text, Markdown)
    }

fun deleteAll(usertag: String) {
    db.deleteAllTimes(usertag)
}

suspend inline fun BehaviourContext.deleteNotificationsCommand() : Job =
    onCommand("deletenotifications") {
        log("delete all additional notifications", it.chat)
        deleteAll((it.chat as PrivateChat).username?.username!!)
        sendTextMessage(it.chat, "All notifications was deleted", Markdown)
    }

suspend inline fun BehaviourContext.linksCommand() : Job =
    onCommand("links") {
        log("check links", it.chat)
        var text = "";
        text += "*ВТОРОЙ КУРС*\n"
        text += "[очередь](https://docs.google.com/spreadsheets/d/1f2tmAL9QWZ2mf4x0VExjrJ0GwxfhJv6mnepjOQgEsTI/edit#gid=0)\n"
        text += "[баллы](https://docs.google.com/spreadsheets/d/e/2PACX-1vQ52PnrWGnJHzy-KAde38XDw_EoEVBzAfAnHYVb_2Mr0x1LXGwgdXZNuNoA-YO01CA96MGbwu5BhSCL/pubhtml?gid=1330913863&single=true)\n"
        text += "\n*ПЕРВЫЙ КУРС*\n"
        text += "[очередь](https://docs.google.com/spreadsheets/d/1qA5bxy6orLWvjUrS88zQz5nGJOs_eq8zMtROmq-HV1U/view#gid=0)\n"
        text += "[баллы](https://docs.google.com/spreadsheets/d/e/2PACX-1vTMff1WQpAk66EMnZyA3cUCQr_2scBkCLEJwwD7dYOmE1oI1XxOMgart8R0LjVj-39fnRi-lI8ixta2/pubhtml?gid=1001500460&single=true)\n"
        sendTextMessage(it.chat, text, Markdown)
    }
