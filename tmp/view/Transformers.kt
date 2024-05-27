package com.odrigo.recognitionappkt.view

import com.odrigo.recognitionappkt.domain.views.SubSampleItemList

class Transformers {
    companion object {
        fun SubSampleToDeletable (viewSubSample: List<SubSampleItemList>): List<DeletableElement<SubSampleItemList>> {
            return viewSubSample.map { a ->
                DeletableElement(false, a)
            }
        }
    }
}