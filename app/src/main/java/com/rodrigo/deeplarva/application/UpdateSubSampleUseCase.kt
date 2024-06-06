package com.rodrigo.deeplarva.application

import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.routes.services.PicturesServices
import com.rodrigo.deeplarva.routes.services.SubSampleServices

class UpdateSubSampleUseCase(
    private val subSampleService: SubSampleServices,
    private val pictureService: PicturesServices
) {

    fun run(subSampleId: Long, callback: (updated: SubSample?) -> Unit) {
        subSampleService.findOne(subSampleId) {
                subSample -> run {
            if(subSample == null) {
                callback(null)
                return@findOne
            }
            pictureService.findProcessedBySubSampleId(subSampleId) {
                    pictures -> run {
                if(pictures.isEmpty()){
                    return@run
                }
                val updated = generateMeasurements(subSample, pictures)
                subSampleService.update(updated) {
                    callback(null)
                }
            }}
        }}
    }

    private fun generateMeasurements(subSample: SubSample, pictures: List<Picture>): SubSample {
        val min = pictures.minOf { it.count }
        val max = pictures.maxOf { it.count }
        val mean = pictures.sumOf { it.count } / pictures.size

        val valuesList = pictures.map { it.count }.distinct()
        val fashionCounts = valuesList.groupingBy { it }.eachCount()
        val mostCommonFashion = fashionCounts.maxByOrNull { it.value }?.key

        return SubSample(
            id=subSample.id,
            isTraining = true,
            min = min.toFloat(),
            max = max.toFloat(),
            mean = mean.toFloat(),
            average = 0f,
            name = subSample.name
        )
    }
}