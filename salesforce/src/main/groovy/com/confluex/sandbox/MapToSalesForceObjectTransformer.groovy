package com.confluex.sandbox

import com.sforce.soap.partner.sobject.SObject
import groovy.util.logging.Slf4j
import org.mule.api.transformer.TransformerException
import org.mule.common.metadata.datatype.DataTypeFactory
import org.mule.transformer.AbstractTransformer


@Slf4j
class MapToSalesForceObjectTransformer extends AbstractTransformer {

    @Override
    protected Object doTransform(Object src, String enc) throws TransformerException {
        def payload = src as List<Map>
        payload.each { record ->
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
        return payload
    }
}
