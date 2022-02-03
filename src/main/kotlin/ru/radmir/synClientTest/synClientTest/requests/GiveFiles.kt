package ru.radmir.synClientTest.synClientTest.requests

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
import java.util.*
import kotlin.collections.ArrayList

@Component
class GiveFiles {
    @Autowired
    private lateinit var storage: Swaydb
    @Autowired
    private lateinit var cryptographer: Cryptographer
    @Autowired
    private lateinit var directoryChecker: DirectoryChecker

    fun start(){
//         получаем файлы, которые нужно запросить
        val giveFiles = storage.get(Vars.otherGiveFiles)!!
            .split(Vars.otherDelimiterIfInStringHasAlreadyDelimiterBetweenNodes).toMutableList()
        giveFiles.removeAt(giveFiles.lastIndex)

        if (giveFiles.isNotEmpty()) {
            val ip = storage.get(Vars.configIp)
            val port = storage.get(Vars.configPort)
            val myName = storage.get(Vars.configMyName)!!
            // val myNameEncrypt = cryptographer.encryptString(myName, Vars.cryptoWhoServer)
            val url = "http://${ip}:${port}/${Vars.netLinkGiveFiles}?name=${myName}" // тут может быть еше один параметр
            val httpPost = HttpPost(url)

//                 конвертируем данные со storage
            //  start encrypt
            val tempRootGiveFilesClientEncrypted = ArrayList<ClientGive>()
            for (i in giveFiles) {
                tempRootGiveFilesClientEncrypted.add(ClientGive(
                    nameDir = cryptographer.encryptString(
                        i.split(Vars.otherDelimiterBetweenComponentsOfNode)[0], Vars.cryptoWhoServer),
                    nameFile = cryptographer.encryptString(
                        i.split(Vars.otherDelimiterBetweenComponentsOfNode)[1], Vars.cryptoWhoServer),
                    sizeFile = cryptographer.encryptString(
                        i.split(Vars.otherDelimiterBetweenComponentsOfNode)[2], Vars.cryptoWhoServer),
                    timeFile = cryptographer.encryptString(
                        i.split(Vars.otherDelimiterBetweenComponentsOfNode)[3], Vars.cryptoWhoServer)
                    )
                )
            }
            //  end encrypt

            val creatorJsonGiveFilesClient = CreatorJsonGiveFilesClient()
                .start(RootGiveFilesClient(clientGive = tempRootGiveFilesClientEncrypted))
            val params = StringEntity(creatorJsonGiveFilesClient)
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
                if (text == Vars.otherEmpty) {
                    println(Vars.netErrorsSomeProblemWithTheServer)
                } else {
                    val rootGiveFilesServer: RootGiveFilesServer = CreatorObjectsGiveFileServer().start(text)
                    createFiles(rootGiveFilesServer, myName)
                    storage.set(Vars.otherGiveFiles, Vars.otherEmpty)
                }
            } else {
                println(Vars.netErrorsServerIsNotAvailable)
                directoryChecker.start(storage.get(Vars.configRootDirectory)!!)
                storage.set(Vars.otherSchema, Vars.otherEmpty)
                storage.set(Vars.newErrorsDoNotSend, Vars.otherBooleanTrue)
            }
        }
    }

    private fun createFiles(rootGiveFilesServer: RootGiveFilesServer, myName: String) {
        val rootGiveFilesServerDecrypted: ArrayList<ServerGive> = arrayListOf()
        // start decrypt
        for (i in rootGiveFilesServer.serverGive) {
            rootGiveFilesServerDecrypted.add(
                ServerGive(
                    pairName = cryptographer.decryptString(i.pairName!!, Vars.cryptoWhoServer),
                    fileName = cryptographer.decryptString(i.fileName!!, Vars.cryptoWhoServer),
                    sizeFile = cryptographer.decryptString(i.sizeFile!!, Vars.cryptoWhoServer),
                    timeFile = cryptographer.decryptString(i.timeFile!!, Vars.cryptoWhoServer),
                    contentOfFile = cryptographer.decryptFile(i.contentOfFile!!,
                        Vars.cryptoWhoClient, cryptographer.decryptString(i.pairName!!, Vars.cryptoWhoServer)
                    )
                )
            )
        }
        // end decrypt

        for(i in rootGiveFilesServerDecrypted) {
            val root = storage.get(Vars.configRootDirectory) + File.separator
            val pathAndName = convertFileName(i.fileName)
            val relativePath = pathAndName[0]
            val fileName = pathAndName[1]
            val pairName = i.pairName
            val contentOfFile = i.contentOfFile!!
            val sizeFile = i.sizeFile!!
            val timeFile = i.timeFile!!
            val folderPath = root +
                    if (pairName == myName) {
                        myName
                    }else {
                        pairName
                    } +
                    if (relativePath == File.separator) {
                        ""
                    } else {
                        relativePath
                    }


            val filePath = folderPath + File.separator + fileName
            // это можно положить в storage
            val deletedFile = File(storage.get(Vars.configRootDirectory) + File.separator + Vars.otherDeletedFiles)
            if (filePath in deletedFile.readText(Charsets.UTF_8)) {
                continue
            }
            val fileIsExists = File(filePath)
            if (fileIsExists.exists()) {

                if (fileIsExists.lastModified() >= timeFile.toLong()) {
                    continue
                } else {
                    try {
                        fileIsExists.writeBytes(Base64.getDecoder().decode(contentOfFile))
                    } catch (e: Exception) {
                        // тут может быть ошибка, если закинуть большой файл
                        // и начать его открыть до завершения загрзки
                        storage.set(Vars.otherUpdateFileDeleteFiles, Vars.otherBooleanTrue)
                    }
                    continue
                }
            }
            try {
                File(folderPath).mkdirs()
                val file = File(filePath)
                file.createNewFile()
                try {
                    file.writeBytes(Base64.getDecoder().decode(contentOfFile))
                } catch (e: Exception) {
                    // тут может быть ошибка, если закинуть большой файл
                    // и начать его открыть до завершения загрзки
                    storage.set(Vars.otherUpdateFileDeleteFiles, Vars.otherBooleanTrue)
                }
            } catch (e: Exception) {
                println("${Vars.otherErrorsFileCreate}: $i. Err: $e")
            }
        }
    }

    private fun convertFileName(nameFile: String?): List<String> {
        val path = nameFile!!.split(Vars.otherDelimiterBetweenNodes)[0]
            .replace(Vars.otherDelimiterInPathOfFile, File.separator)
        val name = nameFile.split(Vars.otherDelimiterBetweenNodes)[1]
        return listOf(path, name)
    }
}