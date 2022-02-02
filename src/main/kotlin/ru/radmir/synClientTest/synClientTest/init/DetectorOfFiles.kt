package ru.radmir.synClientTest.synClientTest.init

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.database.swaydb.Swaydb
import ru.radmir.synClientTest.synClientTest.hashcheck.DirectoryChecker
import ru.radmir.synClientTest.synClientTest.hashcheck.HashChecker
import ru.radmir.synClientTest.synClientTest.requests.CheckUpdateFiles
import ru.radmir.synClientTest.synClientTest.requests.GiveFiles
import java.io.File

@Component
class DetectorOfFiles() {
    @Autowired
    private lateinit var storage: Swaydb
    @Autowired
    private lateinit var hashChecker: HashChecker
    @Autowired
    private lateinit var checkUpdateFiles: CheckUpdateFiles
    @Autowired
    private lateinit var checkFilesForGive: GiveFiles
    @Autowired
    private lateinit var directoryChecker: DirectoryChecker

    fun start(){
        val rootDir = storage.get(Vars.configRootDirectory)!!
        val timeout = storage.get(Vars.configTimeout)!!.toLong()
        detect(rootDir, timeout)
    }
    private fun detect(rootDir: String, timeout: Long) {
        createSchema(rootDir)
        while (true) {
            // проверка файловой системы
            Thread.sleep(timeout)
            // hashChecker.start(rootDir)
            // отключил потому что при работе он мониторит изменения в папке,
            // но измения могут быть так же и на сервере и мы о них не узнаем
            directoryChecker.start(rootDir)
            // get запрос, узнать обновления
            checkUpdateFiles.start()
            // забарать файлы
            checkFilesForGive.start()
        }
    }

    private fun createSchema(path: String) {
        // тут надо добавить для буфера обмена !!!
        // в имени не должно быть &&& и ___ и ... !!!
        // так же дальнейшие директориии не могут иметь такое же название как и главная дректория !!!
        val friends = storage.get(Vars.configFriends)!!.split(Vars.otherDelimiterBetweenNodes)
        for (i in 0..friends.size-2) {
            val tmp = File(path + File.separator + friends[i]
                .split(Vars.otherDelimiterBetweenComponentsOfNode)[0])
            tmp.mkdir()
        }
    }
}