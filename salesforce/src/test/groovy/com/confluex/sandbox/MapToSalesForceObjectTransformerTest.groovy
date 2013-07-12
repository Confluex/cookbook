package com.confluex.sandbox

import com.sforce.soap.partner.sobject.SObject
import org.junit.Test


class MapToSalesForceObjectTransformerTest {

    @Test
    void shouldConvertEmbeddedMapsToSalesForceObjects() {
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
        assert response[0].Account instanceof SObject
    }
}
