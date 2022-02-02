package ru.radmir.synClientTest.synClientTest.requests.json

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.init.Vars
import java.lang.reflect.Type

@Component
data class ClientGive(
    @SerializedName(Vars.jsonNameDirGiveClient) var nameDir: String? = null,
    @SerializedName(Vars.jsonNameFileGiveClient) var nameFile: String? = null,
    @SerializedName(Vars.jsonSizeOfFileGiveClient) var sizeFile: String? = null,
    @SerializedName(Vars.jsonTimeOfFileGiveClient) var timeFile: String? = null
)

@Component
data class RootGiveFilesClient(
    @SerializedName(Vars.jsonClientGiveClient) var clientGive: ArrayList<ClientGive>? = null
)

@Component
class CreatorJsonGiveFilesClient() {
    fun start(test: RootGiveFilesClient): String {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(test::class.java, PutClientAdapter())
        val gson = builder.create()
        return gson.toJson(test)
    }
}
class PutClientAdapter: JsonSerializer<RootGiveFilesClient> {
    override fun serialize(p0: RootGiveFilesClient?, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        val list = JsonArray()
        for (i in p0!!.clientGive!!.iterator()) {
            val jo = JsonObject()
            jo.addProperty(Vars.jsonNameDirGiveClient, i.nameDir)
            jo.addProperty(Vars.jsonNameFileGiveClient, i.nameFile)
            jo.addProperty(Vars.jsonSizeOfFileGiveClient, i.sizeFile)
            jo.addProperty(Vars.jsonTimeOfFileGiveClient, i.timeFile)
            list.add(jo)
        }
        val jsonObject = JsonObject()
        jsonObject.add(Vars.jsonClientGiveClient, list)
        return jsonObject
    }
}
/////////////////////////////////////////////////////////////////////////////////////////////////
@Component
data class ServerGive (
    @SerializedName(Vars.jsonPairNameGiveServer) val pairName : String? = null,
    @SerializedName(Vars.jsonFileNameGiveServer) val fileName : String? = null,
    @SerializedName(Vars.jsonSizeOfFileGiveServer) val sizeFile: String? = null,
    @SerializedName(Vars.jsonTimeOfFileGiveServer) val timeFile: String? = null,
    @SerializedName(Vars.jsonContentOfFileGiveServer) val contentOfFile : String? = null
    )

@Component
data class RootGiveFilesServer (
    @SerializedName(Vars.jsonServerGiveServer) var serverGive: ArrayList<ServerGive> = arrayListOf()
)

@Component
class CreatorObjectsGiveFileServer() {
    fun start(json: String): RootGiveFilesServer {
        val gson = Gson()
        return gson.fromJson(json, RootGiveFilesServer::class.java)
    }
}