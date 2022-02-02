package ru.radmir.synClientTest.synClientTest.init

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.database.swaydb.Swaydb
import java.io.File

@Component
data class NamePassword(
    @SerializedName(Vars.configPairName) var pairName: String? = null,
    @SerializedName(Vars.configPassword) var password: String? = null
)

@Component
data class ConfigClient(
    @SerializedName(Vars.configPort) var port: String? = null,
    @SerializedName(Vars.configIp) var ip: String? = null,
    @SerializedName(Vars.configPasswordServer) var passwordServer: String? = null,
    @SerializedName(Vars.configLocalNetwork) var networkIsLocal: String? = null,
    @SerializedName(Vars.configClipboard) var clipboard: ArrayList<NamePassword>? = null,
    @SerializedName(Vars.configRootDirectory) var rootDir: String? = null,
    @SerializedName(Vars.configMyName) var myName: String? = null,
    @SerializedName(Vars.configFriends) var friends: ArrayList<NamePassword>? = null,
    @SerializedName(Vars.configTimeout) var timeout: String? = null,
    @SerializedName(Vars.configCodeword) var codeword: String? = null
)

@Component
class ReadConfigFromJson() {
    @Autowired
    private lateinit var configClient: ConfigClient
    @Autowired
    private lateinit var storage: Swaydb

    fun start() {
        val file = File(Vars.configName)
        file.createNewFile()
        val json = file.readText(Charsets.UTF_8)
        configClient = Gson().fromJson(json, ConfigClient::class.java)
        checkErrors()
        toStorage()
    }

    private fun checkErrors() {
        if (configClient.ip == Vars.otherAuto && configClient.networkIsLocal == Vars.otherNot ||
            configClient.port == Vars.otherAuto && configClient.networkIsLocal == Vars.otherNot) {
                throw Exception(Vars.configErrorsIpOrPortAutoErr)
        }
        if (configClient.ip == Vars.otherEmpty || configClient.port == Vars.otherEmpty) {
            throw Exception(Vars.configErrorsIpOrPortIsEmpty)
        }
        if (configClient.myName == Vars.otherEmpty) {
            throw Exception(Vars.configErrorsMyNameIsEmpty)
        }
        if (configClient.codeword == Vars.otherEmpty) {
            throw Exception(Vars.configErrorsMyCodewordIsEmpty)
        }
        if (configClient.timeout == Vars.otherEmpty ||
            configClient.timeout!!.toIntOrNull() == null ||
            configClient.timeout!!.toInt() < 100) {
            throw Exception(Vars.configErrorsTimeoutIsEmptyOrUndefined)
        }
    }

    private fun toStorage() {
        storage.set(Vars.configPort, configClient.port!!)
        storage.set(Vars.configIp, configClient.ip!!)
        storage.set(Vars.configPasswordServer, configClient.passwordServer!!)
        storage.set(Vars.configLocalNetwork, configClient.networkIsLocal!!)

        var temp = Vars.otherEmpty
        for (i in configClient.clipboard!!.iterator()) {
            temp += i.pairName!! +
                    Vars.otherDelimiterBetweenComponentsOfNode +
                    i.password!! +
                    Vars.otherDelimiterBetweenNodes
        }
        storage.set(Vars.configClipboard, temp)
        storage.set(Vars.configRootDirectory, configClient.rootDir!!)
        storage.set(Vars.configMyName, configClient.myName!!)

        temp = Vars.otherEmpty
        for (i in configClient.friends!!.iterator()) {
            temp += i.pairName!! +
                    Vars.otherDelimiterBetweenComponentsOfNode +
                    i.password!! +
                    Vars.otherDelimiterBetweenNodes
        }
        storage.set(Vars.configFriends, temp)
        storage.set(Vars.configTimeout, configClient.timeout!!)
        storage.set(Vars.configCodeword, configClient.codeword!!)
    }
}