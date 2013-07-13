package com.confluex.sandbox

import com.sforce.soap.partner.sobject.SObject
import com.sforce.ws.bind.XmlObject
import groovy.util.logging.Slf4j
import org.mule.api.transformer.TransformerException
import org.mule.transformer.AbstractTransformer

@Slf4j
class MapToSalesForceObjectTransformer extends AbstractTransformer {

    @Override
    protected Object doTransform(Object src, String enc) throws TransformerException {
        def list = src as List
        list.each { map(it) }
    }

    protected void map(Map record) {
        def types = record.findAll { it.value instanceof Map }
        types.each { type ->
            log.debug("Converting {} to SObject", type)
            def sObject = new SObject(type: type.key.toString())
            def fields = type.value as Map
            fields.keySet().each { key -> sObject.setField(key.toString(), fields[key]) }
            record[type.key] = sObject
            log.debug("Conversion of {} to SObject complete: {}", type, sObject)
        }
    }


    protected void map(SObject record) {
        def types = record.children.findAll { so -> record.getChild(so.name.localPart).value instanceof Map }
        types.each { XmlObject so ->
            def type = so.name.localPart
            log.debug("Converting {} to SObject", type)
            def sObject = new SObject(type: type)
            def fields = so.value as Map
            fields.keySet().each { key -> sObject.setField(key.toString(), fields[key]) }
            record.setField(type, sObject)
            log.debug("Conversion of {} to SObject complete: {}", type, sObject)
        }

    }

}
