package ru.radmir.synClientTest.synClientTest.requests

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import ru.radmir.synClientTest.synClientTest.database.swaydb.Swaydb
import ru.radmir.synClientTest.synClientTest.encryption.Cryptographer
import ru.radmir.synClientTest.synClientTest.hashcheck.DirectoryChecker
import ru.radmir.synClientTest.synClientTest.init.Vars
import ru.radmir.synClientTest.synClientTest.requests.json.*
import java.io.File


@Controller
class CheckUpdateFiles() {
    @Autowired
    private lateinit var storage: Swaydb
    @Autowired
    private lateinit var creatorJsonUpdatedFiles: CreatorJsonUpdateFiles
    @Autowired
    private lateinit var rootUpdateFiles: RootUpdateServerFiles
    @Autowired
    private lateinit var cryptographer: Cryptographer
    @Autowired
    private lateinit var directoryChecker: DirectoryChecker

    fun start() {
        val ip = storage.get(Vars.configIp)
        // если у нас изменился файл с удаленными файлами
        val deletedFilesIsUpdated = storage.get(Vars.otherUpdateFileDeleteFiles)
        val port = storage.get(Vars.configPort)!!
        val myName = storage.get(Vars.configMyName)!!
        // start encrypt
        // val myNameEncrypted = cryptographer.encryptString(myName, Vars.cryptoWhoServer)
        // stop encrypt
        val url = "http://${ip}:${port}/${Vars.netLinkCheckUpdateFiles}" +
                "?name=${myName}" +
                "&me=${if (deletedFilesIsUpdated.toBoolean()) deletedFilesIsUpdated else "" }"
        storage.set(Vars.otherUpdateFileDeleteFiles, "false")

        val httpGet = HttpGet(url)
        val httpclient = HttpClients.createDefault()
        lateinit var httpResponse: HttpResponse
        try {
            httpResponse = httpclient.execute(httpGet)
        } catch (e:Exception) {
            println(Vars.netErrorsInvalidIpAndPortSettingsOrTheServerIsDown)
            storage.set(Vars.newErrorsDoNotSend, Vars.otherBooleanTrue)
            return
        }

        if (httpResponse.statusLine.statusCode == 200) {
            val text = httpResponse.entity.content.reader().readText()
            if (Vars.netServerResponseUsernameAlreadyTaken in text) {
                throw Exception(Vars.netErrorsUsernameAlreadyTaken)
            }
            if (Vars.netServerResponseNoUpdates in text) {
                return
            }
            if (text == Vars.otherEmpty) {
                // если произойдет удаление в папке, то нужно его поймать
                directoryChecker.start(storage.get(Vars.configRootDirectory)!!)
                storage.set(Vars.otherSchema, Vars.otherEmpty)
                directoryChecker.start(storage.get(Vars.configRootDirectory)!!)
            } else {
                rootUpdateFiles = creatorJsonUpdatedFiles.start(text)
                checkingForFiles(rootUpdateFiles, myName)
            }
        } else {
            println(Vars.netErrorsServerIsNotAvailable)
            storage.set(Vars.newErrorsDoNotSend, Vars.otherBooleanTrue)
        }
    }

    private fun checkingForFiles(rootUpdateFiles: RootUpdateServerFiles, myName: String?) {
        val neededFiles = mutableListOf<UpdateServer>()
        // start decrypt
        val rootUpdateFilesDecrypted: ArrayList<UpdateServer> = arrayListOf()
        for (i in rootUpdateFiles.server) {
            rootUpdateFilesDecrypted.add(
                UpdateServer(
                    nameFile = cryptographer.decryptString(i.nameFile!!, Vars.cryptoWhoServer),
                    nameDir = cryptographer.decryptString(i.nameDir!!, Vars.cryptoWhoServer),
                    sizeFile = cryptographer.decryptString(i.sizeFile!!, Vars.cryptoWhoServer),
                    timeFile = cryptographer.decryptString(i.timeFile!!, Vars.cryptoWhoServer)
                )
            )
        }
        // end decrypt

        for(i in rootUpdateFilesDecrypted) {
            val pathName = convertFileName(i.nameFile)
            val name = pathName[1]
            val nameDir = i.nameDir!!
            // тут наврнео не нужен сепаратор
            val root = storage.get(Vars.configRootDirectory) + File.separator // нужно ли тут File.separator ?
            val relativePath = pathName[0]
            val pairName = if (nameDir.replace(myName!!, Vars.otherEmpty) != Vars.otherUnderscore)
                nameDir.replace(myName, Vars.otherEmpty).replace(Vars.otherUnderscore, Vars.otherEmpty) else myName
            val filePathWithoutRelativePath = root +
                    pairName

            val filePath = filePathWithoutRelativePath +
                     if (relativePath != File.separator) {
                         relativePath + File.separator + name
                     } else {
                         relativePath + name
                     }

            // проверка в удаленных файлах
            val deletedFile = File(storage.get(Vars.configRootDirectory ) + File.separator + Vars.otherDeletedFiles)

            if (!deletedFile.exists()) {
                deletedFile.createNewFile()
                deletedFile.writeText(Vars.otherDeletedFilesText)
            }
            val deletedFiles = deletedFile.readText(Charsets.UTF_8)
            if (filePath !in deletedFiles) {
                if (File(filePath).exists()) {
                    val file = File(filePath)
                    val lastModified = file.lastModified()
                    if (i.timeFile!!.toLong() > lastModified) { // было >=
                        neededFiles.add(i)
                    }
                } else {
                    val friends = storage.get(Vars.configFriends)!!.split(Vars.otherDelimiterBetweenNodes).toMutableList()
                    friends.removeAt(friends.lastIndex)

                    var counter = 0
                    for (i in friends) {
                        if (i.split(Vars.otherDelimiterBetweenComponentsOfNode)[0] in nameDir){
                            counter++
                        }
                    }
                    if (i.nameDir!!.replace(myName, Vars.otherEmpty) == Vars.otherUnderscore || counter > 0) {
                        neededFiles.add(i)
                    }
                }
            } else {
                continue
            }
        }
        // сохранение в storage
        var temp = Vars.otherEmpty
        for (i in neededFiles) {
            temp += i.nameDir + Vars.otherDelimiterBetweenComponentsOfNode +
                    i.nameFile + Vars.otherDelimiterBetweenComponentsOfNode +
                    i.sizeFile + Vars.otherDelimiterBetweenComponentsOfNode +
                    i.timeFile + Vars.otherDelimiterIfInStringHasAlreadyDelimiterBetweenNodes
        }
        storage.set(Vars.otherGiveFiles, temp)
    }

    private fun convertFileName(nameFile: String?): List<String> {
        val path = nameFile!!.split(Vars.otherDelimiterBetweenNodes)[0].replace(Vars.otherDelimiterInPathOfFile, File.separator)
        val name = nameFile.split(Vars.otherDelimiterBetweenNodes)[1]
        return listOf(path, name)
    }
}