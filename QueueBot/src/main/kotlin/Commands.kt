import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.ParseMode.Markdown
import dev.inmo.tgbotapi.types.chat.abstracts.PrivateChat
import kotlinx.coroutines.Job

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
        println("!!!")
        val data = SheetsQuickstart.getQueues()
        val tb = data?.let { it1 -> Table(it1) }
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

val tmpUserMap = mutableMapOf<String, String>(
    "@Quicksmart" to "Будущев Матвей Ярославович")

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
        if (usertag != null) {
            var name = getName(usertag)
            if (name == null)
                name = ""
            val tb = SheetsQuickstart.getQueues()?.let { it1 -> Table(it1) }
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
        if (dm.checkExisting(addInfo)) {
            if (usertag != null) {
                setName(usertag, addInfo)
            }
            sendTextMessage(it.chat, "Your name changed successfully to $addInfo!", Markdown)
        } else {
            sendTextMessage(it.chat, "Unknown student: $addInfo", Markdown)
        }
    }