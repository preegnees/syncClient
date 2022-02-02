package ru.radmir.synClientTest.synClientTest.requests.json

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.init.Vars

@Component
data class UpdateServer (
    @SerializedName(Vars.jsonNameDirUpdate) var nameDir  : String? = null,
    @SerializedName(Vars.jsonFileNameUpdate) var nameFile : String? = null,
    @SerializedName(Vars.jsonSizeOfFileUpdate) var sizeFile: String? = null,
    @SerializedName(Vars.jsonTimeOfFileUpdate) var timeFile: String? = null
)

@Component
data class RootUpdateServerFiles (
    @SerializedName(Vars.jsonServerUpdate) var server : ArrayList<UpdateServer> = arrayListOf()
)

@Component
class CreatorJsonUpdateFiles() {
    fun start(json: String): RootUpdateServerFiles {
        val gson = Gson()
        return gson.fromJson(json, RootUpdateServerFiles::class.java)
    }
}