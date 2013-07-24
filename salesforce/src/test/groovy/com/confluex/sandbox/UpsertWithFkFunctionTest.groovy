package com.confluex.sandbox

import com.sforce.soap.partner.sobject.SObject
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase


class UpsertWithFkFunctionTest extends FunctionalTestCase {
    @Override
    protected String getConfigResources() {
        return "upsert-with-fk.xml"
    }

    @Test
    void shouldUpsertRecordWithFk() {
        def payload = [
                [
                        OriginalEmail__c: "mike@devnull.org",
                        FirstName: "Mike",
                        LastName: "Cantrell",
                        Email: "mike@devnull.org",
                        Account: ["ExternalId__c": 138]
                ]
        ]
        def msg = muleContext.client.send("upsert", payload, [:], 10000)

        assert !muleContext.client.request("errors", 1000)?.exceptionPayload?.exception
        assert msg.payload[0].success == true
    }

    @Test
    void shouldUpsertCustomObjectWithFk() {
        def payload = [
                [
                        Name: "Juno",
                        "ExternalId__c": 100,
                        "Cost__c": 700000000,
                        "Destination__r": [
                                type: "Planet__c",
                                "ExternalId__c": 5
                        ]
                ]
        ]

        def msg = muleContext.client.send("upsert", payload, [:], 10000)

        assert !muleContext.client.request("errors", 1000)?.exceptionPayload?.exception
        assert msg.payload[0].success == true
    }
}
