package com.rodrigo.deeplarva.bussiness

import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.entity.SubSample

class SubSampleLogic {
    companion object {
        fun generateMeasurements(subSample: SubSample, pictures: List<Picture>): SubSample {
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
}