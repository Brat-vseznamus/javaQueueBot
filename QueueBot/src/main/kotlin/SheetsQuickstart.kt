import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.SheetsScopes
import kotlin.Throws
import java.io.IOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.auth.oauth2.Credential
import java.io.FileNotFoundException
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import java.security.GeneralSecurityException
import kotlin.jvm.JvmStatic
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.services.sheets.v4.Sheets
import java.io.File
import java.io.InputStreamReader

object SheetsQuickstart {
    private const val APPLICATION_NAME = "Google Sheets API Java Quickstart"
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    private const val TOKENS_DIRECTORY_PATH = "tokens"
    const val JAVA_SPREAD_SHEETS_ID = "1f2tmAL9QWZ2mf4x0VExjrJ0GwxfhJv6mnepjOQgEsTI"
    const val JAVA_LIST_NAME = "queue"
    const val JAVA_RANGE = "A1:E20"

    const val DM_SPREAD_SHEETS_ID = "1LqrnjZB_QiUzyaCrl78Vn8S7-ouE0IKasBgY2tEfxL8"
    const val DM_LIST_NAME = "Результаты"
    const val DM_RANGE = "A4:A153"

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)
    private const val CREDENTIALS_FILE_PATH = "/credentials.json"

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        // Load client secrets.
        val `in` = SheetsQuickstart::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
            ?: throw FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH)
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Build a new authorized API client service.
        val queues = getQueues();
        if (queues != null) {
            for (row in queues) {
                println(row)
            }
        }
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    @JvmStatic
    fun getQueues() : MutableList<MutableList<Any>>? {
        return getQueues(JAVA_SPREAD_SHEETS_ID, JAVA_LIST_NAME, JAVA_RANGE)
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    @JvmStatic
    fun getQueues(spreadsheetid : String, listName : String, range: String) : MutableList<MutableList<Any>>? {
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val range = "$listName!$range"
        val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build()
        val response = service.spreadsheets().values()[spreadsheetid, range]
            .execute()
        val values = response.getValues()
        if (values == null || values.isEmpty()) {
            println("No data found.")
        }
        return values;
    }

}