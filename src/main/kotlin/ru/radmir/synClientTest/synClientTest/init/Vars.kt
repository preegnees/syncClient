package ru.radmir.synClientTest.synClientTest.init

import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct

@Component
class Vars {
    companion object {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //      config
        const val configName = "config.json"
        const val configPairName = "pairName"
        const val configPassword = "password"
        const val configPort = "port"
        const val configIp = "ip"
        const val configPasswordServer = "password_server"
        const val configLocalNetwork = "network_is_local"
        const val configClipboard = "clipboard"
        const val configRootDirectory = "root_directory"
        const val configMyName = "my_name"
        const val configFriends = "friends"
        const val configTimeout = "timeout_file_searcher_in_milliseconds"
        const val configCodeword = "codeword"
        //          config  errors
        const val configErrorsEnterAllPositionsInConfig = "correctly enter all positions in \"config.json\", \n" +
                "правильно введи все позиции в \"config.json\""
        const val configErrorsLocalNetworkIsNotUndefined = "\"local_environment\" must be \"not\" or \"yes\", \n" +
                "\"local_environment\" должно быть \"not\" или \"yes\""
        const val configErrorsIpOrPortAutoErr = "\"ip\" or \"port\" cannot be \"auto\" if \"local_environment\" is \"not\", \n" +
                "\"ip\" или \"port\" не могут быть \"auto\", если \"local_environment\" равен \"not\""
        const val configErrorsIpOrPortIsEmpty = "\"ip\" or \"port\" is empty, \n" + "\"ip\" или \"port\" пусты"
        const val configErrorsMyNameIsEmpty = "\"my_name\" is empty, \n" + "\"my_name\" пусто"
        const val configErrorsMyCodewordIsEmpty = "\"codeword\" is empty, \n" + "\"codeword\" пусто"
        const val configErrorsTimeoutIsEmptyOrUndefined = "\"timeout\" is empty or not integer number, which > 0, \n" +
                "\"timeout\" пусто или не целое число, которое > 0"
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //      other
        const val otherYes = "yes"
        const val otherNot = "not"
        const val otherEmpty = ""
        const val otherAuto = "auto"
        const val otherDelimiterBetweenComponentsOfNode = "___"
        const val otherDelimiterBetweenNodes = "&&&"
        const val otherHash = "hash"
        const val otherDeletedFiles = "удаленные_deleted.txt"
        const val otherDeletedFilesText = "<<<This is where deleted files are stored. \n" +
                "They can be returned if they are on the server to which you are connected. \n" +
                "To do this, remove the line with the file name.\n\n" +
                "Тут хранятся удаленные файлы. \n" +
                "Их можно вернуть если они есть на сервере, к которому вы подключены. \n" +
                "Для этого нужно удалить строчку с названием файла.>>> \n\n"
        const val otherHashDeletedFiles = "hashDeletedFile"
        const val otherUpdateFileDeleteFiles = "otherUpdateFileDeleteFiles"
        const val otherBooleanTrue = "true"
        const val otherSchema = "schema"
        const val otherUnderscore = "_"
        const val otherDelimiterIfInStringHasAlreadyDelimiterBetweenNodes = "|||"
        const val otherGiveFiles = "giveFiles"
        const val otherDelimiterInPathOfFile = "..."
        const val otherContentType = "content-type"
        const val otherApplicationJson = "application/json"
        const val otherDownloadingFile = "скачивается_downloading"
        const val otherSaveFileSizeOfFile = "sizeOfFile_"
        const val otherSaveTimeUpdateOfFile = "timeUpdateOfFile_"
        const val otherSaveContentOfFile = "contentOfFile_"
        const val otherInvalidSymbolMicrosoftTempFile = "~"
        //          other  errors
        const val otherErrorsStrangeFileInRootDirectory =
            "in the root directory there should be only folders whose names you described in the config.json, \n" +
                    "в корневом каталоге должны быть только папки, имена которых вы осписали в config.json"
        const val otherErrorsFileCreate = "file creation error, \n" + "ошибка создания файла"
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //          json
        //          json    give
        const val jsonNameDirGiveClient = "name_dir"
        const val jsonNameFileGiveClient = "name_file"
        const val jsonClientGiveClient = "client"
        const val jsonServerGiveServer = "server"
        const val jsonPairNameGiveServer = "pair_name"
        const val jsonFileNameGiveServer = "file_name"
        const val jsonContentOfFileGiveServer = "content_of_file"
        const val jsonSizeOfFileGiveClient = "size_file"
        const val jsonTimeOfFileGiveClient = "time_file"
        const val jsonSizeOfFileGiveServer = "size_file"
        const val jsonTimeOfFileGiveServer = "time_file"
        //          json    put
        const val jsonNamePutClient = "name"
        const val jsonPairNamePutClient = "pair_name"
        const val jsonClientPutClient = "client"
        const val jsonFileNamePutClient = "file_name"
        const val jsonContentOfFilePutClient = "content_of_file"
        const val jsonSizeOfFilePutClient = "size_file"
        const val jsonTimeOfFilePutClient = "time_file"
        //          json    update
        const val jsonNameDirUpdate = "name_dir"
        const val jsonFileNameUpdate = "name_file"
        const val jsonSizeOfFileUpdate = "size_file"
        const val jsonTimeOfFileUpdate = "time_file"
        const val jsonServerUpdate = "server"
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //          workers with net (server)
        const val netServerResponseOk = "<<100>>" // все хорошо
        const val netServerResponseUsernameAlreadyTaken = "<<-100>>" // имя пользовтеля уже занято
        const val netServerResponseNoUpdates = "<<-200>>" // не было обновлений на сервере
        const val netLinkCheckUpdateFiles = "check_update_files"
        const val netLinkGiveFiles = "give_files"
        const val netLinkPutFiles = "put_files"
        //          workers with net (server)  errors
        const val netErrorsInvalidIpAndPortSettingsOrTheServerIsDown = "invalid ip and port settings or the server is down, \n" +
                "неверные настройки ip и port или сервер выключен"
        const val netErrorsUsernameAlreadyTaken = "your username is already taken, \n" + "ваше имя пользовтеля уже занято"
        const val netErrorsServerIsNotAvailable = "server is not available, \n" + "сервер недоступен"
        const val netErrorsSomeProblemWithTheServer = "some problem with the server, \n" + "какая то проблема с сервером"
        const val newErrorsDoNotSend = "newErrorsDoNotSend"
        //          crypto
        const val cryptoMethodCipher = "AES"
        const val cryptoWhoClient = "client"
        const val cryptoWhoServer = "server"
    }
}