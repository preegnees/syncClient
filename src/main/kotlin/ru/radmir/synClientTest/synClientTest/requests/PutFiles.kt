package ru.radmir.synClientTest.synClientTest.requests

import java.util.Base64
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.database.swaydb.Swaydb
import ru.radmir.synClientTest.synClientTest.encryption.Cryptographer
import ru.radmir.synClientTest.synClientTest.hashcheck.DirectoryChecker
import ru.radmir.synClientTest.synClientTest.init.Vars
import ru.radmir.synClientTest.synClientTest.requests.json.*
import java.io.File

@Component
class PutFiles {
    @Autowired
    private lateinit var storage: Swaydb
    @Autowired
    private lateinit var cryptographer: Cryptographer

    fun start(updatedFiles: MutableList<List<String>>) {
        val myName = storage.get(Vars.configMyName)!!

        val listForSend: ArrayList<ClientPut> = arrayListOf()
        for (i in updatedFiles) {
            // есть ли тут запрещенный символ
            if (Vars.otherInvalidSymbolMicrosoftTempFile in i[0]){
                continue
            }
            // существует ли файл ?
            val file = File(i[0])
            if (file.exists()) {
                // separator тоже в константы
                val root = storage.get(Vars.configRootDirectory)!!

                val pairName = file.path.split(root)[1].split(File.separator)[1]
                val fileName = file.name
                val relativePath = if (file.path.split(root + File.separator + pairName)[1] == File.separator + fileName) {
                    Vars.otherDelimiterInPathOfFile
                } else {
                    file.path.split(root + File.separator + pairName)[1]
                        .replace(File.separator, Vars.otherDelimiterInPathOfFile)
                        .replace(Vars.otherDelimiterInPathOfFile + fileName, "")
                }

                val contentOfFile = Base64.getEncoder().encodeToString(file.inputStream().readBytes())
                val sizeFile = file.length().toString()
                val timeFile = file.lastModified().toString()
                listForSend.add(
                    ClientPut(
                    name = myName,
                    pairName = pairName,
                    fileName = "$relativePath${Vars.otherDelimiterBetweenNodes}$fileName",
                    sizeFile = sizeFile,
                    timeFile = timeFile,
                    contentOfFile = contentOfFile)
                )
            }
        }
        //  start encrypt
        val tempRootPutFiles = ArrayList<ClientPut>()
        for (i in listForSend) {
            tempRootPutFiles.add(
                ClientPut(
                    name = cryptographer.encryptString(i.name!!, Vars.cryptoWhoServer),
                    pairName = cryptographer.encryptString(i.pairName!!, Vars.cryptoWhoServer),
                    fileName = cryptographer.encryptString(i.fileName!!, Vars.cryptoWhoServer),
                    sizeFile = cryptographer.encryptString(i.sizeFile!!, Vars.cryptoWhoServer),
                    timeFile = cryptographer.encryptString(i.timeFile!!, Vars.cryptoWhoServer),
                    contentOfFile = cryptographer.encryptFile(Base64.getDecoder().decode(i.contentOfFile!!),
                        Vars.cryptoWhoClient, i.pairName!!)
                )
            )
        }
        //  end encrypt
        val json = CreatorJsonPutFiles().start(RootPutFiles(tempRootPutFiles))
        sendJson(json, myName)
    }

    private fun sendJson(json: String, myName: String) {
        // start encrypt
        // val myNameEncrypt = cryptographer.encryptString(myName, Vars.cryptoWhoServer)
        // end encrypt
        val ip = storage.get(Vars.configIp)
        val port = storage.get(Vars.configPort)
        val url = "http://${ip}:${port}/${Vars.netLinkPutFiles}?name=${myName}" // может еще один параметр
        val httpPost = HttpPost(url)
        val params = StringEntity(json)
        httpPost.addHeader(Vars.otherContentType, Vars.otherApplicationJson)
        httpPost.entity = params
        lateinit var httpResponse: HttpResponse
        val httpClient = HttpClients.createDefault()
        try {
            httpResponse = httpClient.execute(httpPost)
        } catch (e: Exception) {
            println(Vars.netErrorsInvalidIpAndPortSettingsOrTheServerIsDown)
            storage.set(Vars.otherSchema, Vars.otherEmpty)
            storage.set(Vars.newErrorsDoNotSend, Vars.otherBooleanTrue)
            return
        }
        if (httpResponse.statusLine.statusCode == 200) {
            val text = httpResponse.entity.content.reader().readText()
            if (Vars.netServerResponseUsernameAlreadyTaken in text) {
                throw Exception(Vars.netErrorsUsernameAlreadyTaken)
            }
            if (Vars.netServerResponseOk !in text) {
                println(Vars.netErrorsSomeProblemWithTheServer)
            }
        } else {
            println(Vars.netErrorsServerIsNotAvailable)
            storage.set(Vars.otherSchema, Vars.otherEmpty)
            storage.set(Vars.newErrorsDoNotSend, Vars.otherBooleanTrue)
        }
    }
}