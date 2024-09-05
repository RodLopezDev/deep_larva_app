package com.iiap.deeplarva.domain.view

import com.iiap.deeplarva.domain.entity.BoxDetection
import com.iiap.deeplarva.domain.entity.Picture
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Picture", strict = false)
data class ExportableDataPicture (
    @field:Element(name = "Metadata")
    val picture: PictureData,
    @field:ElementList(name = "Boxes")
    val boxes: List<BoxDetectionData>,
    @field:Element(name = "Picture")
    val mainPicture: String,
    @field:Element(name = "Processed")
    val processedPicture: String,
) {
    data class PictureData (
        @field:Element(name = "DeviceId")
        val deviceId: String,
        @field:Element(name = "UUID")
        val uuid: String,
        @field:Element(name = "Count")
        val count: Int,
        @field:Element(name = "Time")
        val time: Long,
        @field:Element(name = "Timestamp")
        val timestamp: Long
    ) {
        companion object {
            fun build(picture: Picture): PictureData {
                return PictureData(picture.deviceId, picture.uuid, picture.count, picture.time, picture.timestamp)
            }
        }
    }
    data class BoxDetectionData (
        @field:Element(name = "V1")
        val v1: Int,
        @field:Element(name = "V2")
        val v2: Int,
        @field:Element(name = "V3")
        val v3: Int,
        @field:Element(name = "V4")
        val v4: Int,
    ) {
        companion object {
            fun build(box: BoxDetection): BoxDetectionData {
                return BoxDetectionData(box.v1, box.v2, box.v3, box.v4)
            }
            fun buildList(boxes: List<BoxDetection>): List<BoxDetectionData> {
                return boxes.map { build(it) }
            }
        }
    }
}