package com.iiap.deeplarva.utils

import com.iiap.deeplarva.domain.view.ExportableDataPicture
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.StringWriter

class XmlUtils {
    companion object {
        fun serializeToXml(person: ExportableDataPicture): String {
            val serializer: Serializer = Persister()
            val result = StringWriter()
            serializer.write(person, result)
            return result.toString()
        }
    }
}