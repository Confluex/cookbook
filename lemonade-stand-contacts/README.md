# Background

Little Timmy has a lemonade stand. He's working on marketing his product and noticed that nearly half of his customers are paying via credit card with his nifty new mobile payment app. Timmy would love to capture the contacts in his SalesForce.com account. This will allow him to start a marketing campaign to let his customers know when and where he'll be setting up next!

# A Mule Implementation

In this example, we'll be using Mule to connect to Timmy's gmail acccount. He gets BCC'd with a copy of the receipt that the customers get. This will allow us to:

 - Poll his gmail account for new receipts
 - Parse the contact information (First Name, Last Name, Email)
 - We'll also categorize the contact for him by hard coding the LeadSource field as Lemonade Customer
 - To help him remember their last order, we'll use the email body and insert it into the Contact's description

**Gmail to SalesForce Flow Diagram**

![Gmail to SalesForce Flow Diagram](src/main/docs/flow.jpg?raw=true)

[View the Source](src/main/app/lemonade-stand-contacts.xml)


# First Some SalesForce Setup

We'll be using the Mule Salesforce Connector and using the upsert function (creates a new record or updates an existing). But first, we need to correlate identity between the GMail account and the SalesForce account. This is called an ExternalID field. We'll use the email address.

In order to do this, you'll have to create an ExternalID field inside of SalesForce. This can be done in just a few, simple steps:


**#1 - Find the Setup Menu under your Account Menu**

![Setup Menu](src/main/docs/contacts-external-id-1.jpg?raw=true)

**#2 - Select the 'Customize/Contacts/Fields' option from the left nav**

![Contact Fields](src/main/docs/contacts-external-id-2.jpg?raw=true)

**#3 - Add the Custom Field**
![Custom Field](src/main/docs/contacts-external-id-3.jpg?raw=true)

# Setting up the SalesForce Endpoint

Once we have the new field in place, ensure that Mule has the correct endpoint settings:

 - The sObject type should be set to Contact
 - The External ID field should match your configured field

![SalesForce Endpoint Dialog](src/main/docs/contacts-upsert-dialog.jpg?raw=true)

*XML Source*

```xml
<sfdc:upsert config-ref="Salesforce"  doc:name="Salesforce" externalIdFieldName="OriginalEmail__c" type="Contact"> 			
    <sfdc:objects ref="#[payload]"/>
</sfdc:upsert>
```

# A Word About Payloads, Filters and Transformers

When the message arrives from Gmail, it has useful variables in the message headers. Here are just a few inbound properties which we can use to build transform our payload for SalesForce:

```
    bccAddresses=Mule Test <mule.test@acme.com>
    ccAddresses=
    contentType=multipart/alternative; boundary=089e013cb962bdb55b04dd09ca19
    fromAddress=Timmy Smith <timmy@awesomelemons.com>
    replyToAddresses=Timmy Smith <mtimmy@awesomelemons.com>
    sentDate=Sat May 18 22:27:12 CDT 2013
    subject=Receipt
    toAddresses=Bill Murray <bmurray@acme.com>
```

SalesForce Upserts expects a List<Map> payload with the maps containing values for the fields you wish to update. In order to convert the payload, we've created a simple transformer by extending Mule's AbstractMessageTransformer (extend AbstractTransformer if you only want the payload).
	
```java
/**
 * Custom transformer which will read the inbound message properties from an email
 * message and convert it into a list of maps (contacts) for SalesForce.
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
```	

When it comes out the other end, we have our List of Maps to send to SalesForce!

# Sending the Email

To emulate Timmy's fancy credit card app, we'll just have to send an email manually. Go ahead and just send an email and BCC the account which is being monitored.

![Sending an test email](src/main/docs/send-email.jpg?raw=true)

# Verify the Update in SalesForce

The flow should pick it up and create or update the SalesForce contact:

![SalesForce Results](src/main/docs/upsert-result.jpg?raw=true)

# Example Provided By Confluex

For more information on Confluex and how we can help your organization overcome your integration challenges, please visit our website at [http://www.confluex.com](http://www.confluex.com).
