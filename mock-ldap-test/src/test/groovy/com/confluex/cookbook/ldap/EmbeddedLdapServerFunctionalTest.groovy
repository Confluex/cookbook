package com.confluex.cookbook.ldap

import org.junit.BeforeClass
import org.junit.Test
import org.mule.tck.junit4.FunctionalTestCase

class EmbeddedLdapServerFunctionalTest extends FunctionalTestCase {

    @Override
    protected String getConfigResources() {
        return "ldap-server-context.xml, ldap-test.xml"
    }


    @Test
    void shouldCheckForDnExistence() {
        def msg = muleContext.client.send("ldapDnExists", "uid=ben,ou=people,dc=springframework,dc=org", [:])
        assert msg.payload == true
    }
}
