package com.confluex.sandbox

import com.sforce.soap.partner.sobject.SObject
import com.sforce.ws.bind.XmlObject
import org.junit.Test


class MapToSalesForceObjectTransformerTest {

    @Test
    void shouldConvertEmbeddedMapsToSalesForceObjectsWithListOfMaps() {
        def payload = [
                [
                        OriginalEmail__c: "mike@devnull.org",
                        FirstName: "Mike",
                        LastName: "Cantrell",
                        Email: "mike@devnull.org",
                        Account: [ ExternalId__c:138]
                ]
        ]
        def response = new MapToSalesForceObjectTransformer().doTransform(payload, null) as List<Map>
        def account = response[0].Account as SObject
        assert account.getChild("ExternalId__c").value == 138
    }

    @Test
    void shouldConvertEmbeddedMapsToSalesForceObjectsWithListOfSalesForceObjects() {
        def payload = new SObject(type:"Contact")
        payload.setField("OriginalName_c", "mike@devnull.org")
        payload.setField("FirstName", "Mike")
        payload.setField("LastName", "Cantrell")
        payload.setField("Email", "mike@devnull.org")
        payload.setField("Account", [ ExternalId__c:138])
        def response = new MapToSalesForceObjectTransformer().doTransform([payload], null) as List<XmlObject>
        def account = response[0].getField("Account") as XmlObject
        assert account.getChild("ExternalId__c").value == 138
    }
}
