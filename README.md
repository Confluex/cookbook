# Background

Little Timmy has a lemonade stand. He's working on marketing his product and noticed that nearly half of his customers are paying via credit card with his nifty new mobile payment app. Timmy would love to capture the contacts in his SalesForce.com account. This will allow him to start a marketing campaign to let his customers know when and where he'll be setting up next!

# A Mule Implementation

In this example, we'll be using Mule to connect to Timmy's gmail acccount. He gets BCC'd with a copy of the receipt that the customers get. This will allow us to:

 - Poll his gmail account for new receipts
 - Parse the contact information (First Name, Last Name, Email)
 - We'll also categorize the contact for him by hard coding the LeadSource field as Lemonade Customer
 - To help him remember their last order, we'll use the email body and insert it into the Contact's description

*Gmail to SalesForce Flow Diagram*

![Gmail to SalesForce Flow Diagram](src/main/docs/flow?raw=true)

*SalesForce endpoint Dialog*

![Gmail to Salesforce Flow Diagram](src/main/docs/flow?raw=true)

# First some SalesForce Setup

We'll be using the Mule Salesforce Connector and using the upsert function (creates a new record or updates an existing). But first, we need to correlate identity between the GMail account and the SalesForce account. This is called an ExternalID field. We'll use the email address.

In order to do this, you'll have to create an ExternalID field inside of SalesForce

![Setup Menu](src/main/docs/contacts-external-id-1.jpg?raw=true)
![Contact Fields](src/main/docs/contacts-external-id-2.jpg?raw=true)
![Custom Field](src/main/docs/contacts-external-id-2.jpg?raw=true)

Once we have the new field in place, we're ready to start sending emails.

# Sending the Emails

To emulate Timmy's fancy credit card app, we'll just have to send an email manually. Go ahead and just send an email and BCC the account which is being monitored.

![Sending an test email](src/main/docs/send-email.jpg?raw=true)

The flow should pick it up and create or update the SalesForce contact:

![SalesForce Results](src/main/docs/update-result.jpg?raw=true)