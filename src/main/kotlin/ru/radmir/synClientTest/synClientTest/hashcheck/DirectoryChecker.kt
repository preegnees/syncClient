package ru.radmir.synClientTest.synClientTest.hashcheck

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.database.swaydb.Swaydb
import ru.radmir.synClientTest.synClientTest.init.Vars
import ru.radmir.synClientTest.synClientTest.requests.PutFiles
import java.io.File
import java.util.*
import kotlin.math.abs

@Component
class DirectoryChecker {
    @Autowired
    private lateinit var storage: Swaydb
    @Autowired
    private lateinit var putFiles: PutFiles
    private val newSchema: MutableList<List<String>> = mutableListOf()

    fun start(path: String) {
        getDirs(path)
        checkSchema()
    }

    private fun checkSchema() {
        val allExistsFiles: MutableSet<List<String>> = mutableSetOf()
        for (j in newSchema.indices) {
            val path = newSchema[j][0]
            val file = File(path)
            if (!file.exists()) {
                newSchema.clear()
                start(path)
            }
            allExistsFiles.add(newSchema[j])
        }

        val files = searchFiles()
        val upgradeFiles = files[0]
        val deleteFiles = files[1]
        if (!upgradeFiles.isNullOrEmpty()) {
            // тут делаем запрос
            putFiles.start(upgradeFiles)
        }

        val file = File(storage.get(Vars.configRootDirectory) + File.separator + Vars.otherDeletedFiles)
        if (!deleteFiles.isNullOrEmpty()) {
            // тут записываем в файл (где прописаны удаленные файлы), который будем отправлять
            if (!file.exists()) {
                file.createNewFile()
                file.writeText(Vars.otherDeletedFilesText)
            }
            for (i in deleteFiles) {
                if (Vars.otherInvalidSymbolMicrosoftTempFile !in i[0]) {
                    file.appendText(
                        "path: ${i[0]}, hashCode: ${i[1]}, timeLastUpdate: ${Date(i[2].toLong())}, sizeInBytes: ${i[3]};\n"
                    )
                }
            }
        }
        // проверка на изменения файла с удаленными файлами
        val newHashDeletedFiles = file.hashCode().toString() + file.lastModified().toString()
        val oldHashDeletedFile = storage.get(Vars.otherHashDeletedFiles)
        if (oldHashDeletedFile != newHashDeletedFiles) {
            storage.set(Vars.otherUpdateFileDeleteFiles, Vars.otherBooleanTrue)
            storage.set(Vars.otherHashDeletedFiles, newHashDeletedFiles)
        }

        var temp = Vars.otherEmpty
        for (i in newSchema.iterator()) {
            // 0 = path, 1 = hashCode, 2 = machineDate, 3 = size of file
            temp += i[0] + Vars.otherDelimiterBetweenComponentsOfNode +
                    i[1] + Vars.otherDelimiterBetweenComponentsOfNode +
                    i[2] + Vars.otherDelimiterBetweenComponentsOfNode +
                    i[3] + Vars.otherDelimiterBetweenNodes
        }
        storage.set(Vars.otherSchema, temp)
        newSchema.clear()
    }

    private fun searchFiles(): MutableList<MutableList<List<String>>> {
        var deletedFiles = mutableListOf<List<String>>()
        var updatedFiles = mutableListOf<List<String>>()
        val oldSchemaStorage = storage.get(Vars.otherSchema)!!.split(Vars.otherDelimiterBetweenNodes).toMutableList()
        oldSchemaStorage.removeAt(oldSchemaStorage.lastIndex)

        if (oldSchemaStorage.isEmpty() || oldSchemaStorage[0] == Vars.otherEmpty){
            updatedFiles = newSchema
            val updatedAndDeleted = mutableListOf<MutableList<List<String>>>()
            updatedAndDeleted.add(updatedFiles)
            updatedAndDeleted.add(deletedFiles)
            return updatedAndDeleted
        } else {
            // конвертруем storageOldSchema в номрлаьный список
            val oldSchema = mutableListOf<List<String>>()
            for (i in oldSchemaStorage) {
                oldSchema.add(listOf(
                    i.split("___")[0], // 0 = path,
                    i.split("___")[1], // 1 = hashCode,
                    i.split("___")[2], // 2 = machineDate,
                    i.split("___")[3])) // 3 = size of file
            }
            oldSchemaStorage.clear()

            for (j in oldSchema.iterator()) {
                for (i in newSchema.iterator()) {
                    if (i[0] == j[0]) { // сравниваем пути
                        if (i[1] != j[1]) { // сравниваем хеш
                            updatedFiles.add(listOf(i[0], i[1], i[2], i[3]))
                        }
                    } else {
                        if (i[2] == j[2]) { // сравниваем время создания
                            updatedFiles.add(listOf(i[0], i[1], i[2], i[3]))
                        }
                    }
                }
            }
            for (i in newSchema) {
                if (i !in oldSchema) {
                    if (i !in updatedFiles) {
                        updatedFiles.add(i)
                    }
                }
            }
            val tempOldSchemaWithoutSize = mutableListOf<List<String>>()
            for (i in oldSchema) {
                tempOldSchemaWithoutSize.add(
                    listOf(i[0])
                )
            }
            val tempNewSchemaWithoutSize = mutableListOf<List<String>>()
            for (i in newSchema) {
                tempNewSchemaWithoutSize.add(
                    listOf(i[0])
                )
            }
            for (i in tempOldSchemaWithoutSize) {
                if (i !in tempNewSchemaWithoutSize) {
                    for (j in oldSchema) {
                        if (i[0] == j[0]) {
                            deletedFiles.add(j)
                        }
                    }
                }
            }
            return mutableListOf(updatedFiles, deletedFiles)
        }
    }

    private fun getDirs(path: String) {
        val listOfFiles = File(path).listFiles()
        if (!listOfFiles.isNullOrEmpty()) {
            for (i in listOfFiles){
                if (i.isDirectory){
                    getDirs(i.path)
                }
                if (i.isFile && Vars.otherDeletedFiles.split(".")[0] !in i.name) {
                    newSchema.add(mutableListOf(i.path.toString(),
                        abs(i.hashCode()).toString() +
                                abs(i.lastModified().hashCode()).toString(),
                        i.lastModified().toString(),
                        i.length().toByte().toString())) // нужно ли toByte ?
                }
            }
        }
    }
}
