package ru.radmir.synClientTest.synClientTest.requests.json

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.init.Vars
import java.lang.reflect.Type

@Component
data class ClientPut(
    @SerializedName(Vars.jsonNamePutClient) val name : String? = null,
    @SerializedName(Vars.jsonPairNamePutClient) val pairName : String? = null,
    @SerializedName(Vars.jsonFileNamePutClient) val fileName : String? = null,
    @SerializedName(Vars.jsonSizeOfFilePutClient) val sizeFile: String? = null,
    @SerializedName(Vars.jsonTimeOfFilePutClient) val timeFile: String? = null,
    @SerializedName(Vars.jsonContentOfFilePutClient) val contentOfFile : String? = null
)

@Component
data class RootPutFiles(
    @SerializedName(Vars.jsonClientPutClient) var client: ArrayList<ClientPut>? = null
)

@Component
class CreatorJsonPutFiles() {
    fun start(rootPutFiles: RootPutFiles): String {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(rootPutFiles::class.java, PutAdapter())
        val gson = builder.create()
        return gson.toJson(rootPutFiles)
    }
}
class PutAdapter: JsonSerializer<RootPutFiles> {
    override fun serialize(p0: RootPutFiles?, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        val list = JsonArray()
        for (i in p0!!.client!!.iterator()) {
            val jo = JsonObject()
            jo.addProperty(Vars.jsonNamePutClient, i.name)
            jo.addProperty(Vars.jsonPairNamePutClient, i.pairName)
            jo.addProperty(Vars.jsonFileNamePutClient, i.fileName)
            jo.addProperty(Vars.jsonSizeOfFilePutClient, i.sizeFile!!)
            jo.addProperty(Vars.jsonTimeOfFilePutClient, i.timeFile!!)
            jo.addProperty(Vars.jsonContentOfFilePutClient, i.contentOfFile)
            list.add(jo)
        }
        val jsonObject = JsonObject()
        jsonObject.add(Vars.jsonClientPutClient, list)
        return jsonObject
    }
}
