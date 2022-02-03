package ru.radmir.synClientTest.synClientTest.hashcheck

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.database.swaydb.Swaydb
import ru.radmir.synClientTest.synClientTest.init.Vars
import java.io.File
import kotlin.math.abs

@Component
class HashChecker() {
    @Autowired
    private lateinit var storage: Swaydb
    @Autowired
    private lateinit var directoryChecker: DirectoryChecker

    private var newHash = Vars.otherHash

    fun start(root: String){
        val isNewHash: Boolean = if (storage.get(Vars.newErrorsDoNotSend).toBoolean()) {
            true
        } else {
            hash(root)
        }
        if (isNewHash) {
            directoryChecker.start(root)
        }
    }
    private fun hash(path: String): Boolean {
       getDirs(path, path)
       val oldHash =  try { storage.get(Vars.otherHash) } catch (e: Exception) { Vars.otherEmpty }
       return if (newHash == oldHash) {
           newHash = Vars.otherEmpty
           false
       } else {
           storage.set(Vars.otherHash, newHash)
           newHash = Vars.otherEmpty
           true
       }
    }
    private fun getDirs(path: String, root: String){
        val listOfFiles = File(path).listFiles()
        if (!listOfFiles.isNullOrEmpty()) {
            for (i in listOfFiles){
                if (i.isDirectory){
                    getDirs(i.path, root)
                }
                if (i.isFile && path == root && Vars.otherDeletedFiles !in i.name) {
                    throw Exception(Vars.otherErrorsStrangeFileInRootDirectory)
                }
                if (i.isFile) {
                    if (Vars.otherInvalidSymbolMicrosoftTempFile !in i.name) {
                        newHash += (abs(i.hashCode()).toString() +
                                abs(i.lastModified().hashCode()).toString())
                    }
                }
            }
        }
    }
}