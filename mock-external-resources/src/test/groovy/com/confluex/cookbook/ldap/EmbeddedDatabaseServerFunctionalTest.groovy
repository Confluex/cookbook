package com.confluex.cookbook.ldap

import org.junit.BeforeClass
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase


class EmbeddedDatabaseServerFunctionalTest extends FunctionalTestCase {

    @BeforeClass
    static void enableVerboseErrors() {
        System.setProperty("mule.verbose.exceptions", "true")
    }

    @Override
    protected String getConfigResources() {
        return "jdbc-config.xml"
    }

    @Test
    void shouldLookupUsersFromInClause() {
        def msg = muleContext.client.send("getUserInfo", "bmurray", [:], 10000)
        assert msg.payload == [[USER_NAME:'bmurray', LAST_NAME:'Murray', FIRST_NAME:'Bill']]
    }
}
