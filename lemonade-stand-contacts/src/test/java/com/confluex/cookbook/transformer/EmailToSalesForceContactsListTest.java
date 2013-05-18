package com.confluex.cookbook.transformer;

import static org.junit.Assert.*;
import static com.confluex.cookbook.transformer.EmailToSalesForceContactsList.*;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class EmailToSalesForceContactsListTest {
	EmailToSalesForceContactsList transformer;
	
	@Before
	public void createTransformer() {
		transformer = new EmailToSalesForceContactsList();
	}
	
	@Test
	public void shouldParseSingleEmailToListOfContacts() throws Exception {
		List<Map<String, String>> contacts = transformer.createContactsFromEmail("Mike Cantrell <mike.humansonly@devnull.org>");
		assertEquals(1, contacts.size());
		Map<String, String> contact = contacts.get(0);
		assertEquals("Mike", contact.get(FIELD_FIRST_NAME));
		assertEquals("Cantrell", contact.get(FIELD_LAST_NAME));
		assertEquals("mike.humansonly@devnull.org", contact.get(FIELD_EMAIL));
		assertEquals("mike.humansonly@devnull.org", contact.get(FIELD_EXTERNAL_ID));
		assertEquals(VALUE_LEAD_SOURCE, contact.get(FIELD_LEAD_SOURCE));
	}
	
}
