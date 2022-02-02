package ru.radmir.synClientTest.synClientTest.init

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.requests.CheckUpdateFiles
import javax.annotation.PostConstruct

@Component
class Init() {
    @Autowired
    private lateinit var confRead: ReadConfigFromJson
    @Autowired
    private lateinit var detectorOfFiles: DetectorOfFiles

    @PostConstruct
    fun start(){
        confRead.start()
        detectorOfFiles.start()
    }
}