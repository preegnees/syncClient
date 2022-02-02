package ru.radmir.synClientTest.synClientTest.database.swaydb

import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.init.Vars
import swaydb.java.Map
import swaydb.java.memory.MemoryMap
import swaydb.java.serializers.Default.stringSerializer

@Component
class Swaydb {
    private var map: Map<String, String, Void>? = MemoryMap
        .functionsOff(stringSerializer(), stringSerializer())
        .get()
    fun set(key: String, value: String){
        map?.put(key, value)
    }
    fun get(key: String): String? {
        return try{
            map?.get(key)?.get()
        } catch (e: Exception){
            Vars.otherEmpty
        }
    }
}