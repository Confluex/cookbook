package com.confluex.cookbook.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transformer.types.DataTypeFactory;

/**
 * Custom transformer which will read the inbound message properties from an email
 * message and convert it into a list of maps (contacts) for SalesForce.
 * 
 * @author mcantrell
 */
public class EmailToSalesForceContactsList extends AbstractMessageTransformer {

	public static final String FIELD_FIRST_NAME = "FirstName";
	public static final String FIELD_LAST_NAME = "LastName";
	public static final String FIELD_EMAIL = "Email";
	public static final String FIELD_EXTERNAL_ID = "OriginalEmail__c";
	public static final String FIELD_LEAD_SOURCE = "LeadSource";
	public static final String FIELD_DESCRIPTION = "Description";
	
	public static final String VALUE_LEAD_SOURCE = "Lemonade Customer";
	
	EmailToSalesForceContactsList() {
		setReturnDataType(DataTypeFactory.create(List.class));
	}

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		try {
			List<Map<String, String>> contacts = createContactsFromEmail((String) message.getInboundProperty("toAddresses"));
			for (Map<String, String> contact : contacts) {
				contact.put(FIELD_DESCRIPTION, message.getPayloadAsString());
			}
			return contacts;
		} catch (Exception e) {
			throw new TransformerException(this, e);
		}
	}

	/**
	 * Create the customer from the message meta data
	 */
	protected List<Map<String, String>> createContactsFromEmail(String email) throws AddressException {
		InternetAddress address = new InternetAddress(email);
		List<Map<String, String>> contacts = new ArrayList<Map<String, String>>();
		Map<String, String> contact = new HashMap<String, String>();
		String name = address.getPersonal();
		contact.put(FIELD_FIRST_NAME, parseFirstName(name));
		contact.put(FIELD_LAST_NAME, parseLastName(name));
		contact.put(FIELD_EMAIL, address.getAddress());
		contact.put(FIELD_LEAD_SOURCE, VALUE_LEAD_SOURCE);
		contact.put(FIELD_EXTERNAL_ID, address.getAddress());
		contacts.add(contact);

		return contacts;
	}

	/**
	 * Split the name up and use all of the fields except the last. 
	 * This isn't really very reliable but it gets the point across.
	 */
	protected String parseFirstName(String name) {
		String[] values = name.split("\\W+");
		Object[] firstNameElements = ArrayUtils.subarray(values, 0,
				values.length - 1);
		return StringUtils.join(firstNameElements);
	}

	/**
	 * Split the name and use the last field. Again, not really too reliable
	 * but it's a demo :-)
	 */
	protected String parseLastName(String name) {
		String[] values = name.split("\\W+");
		return values[values.length - 1];
	}

}
