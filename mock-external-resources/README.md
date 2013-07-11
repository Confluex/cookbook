# Mocking External Resources

When it comes to testing mule, it's often difficult to implement integration tests due to the interaction with remote
resources configured within your flows. This project has several examples of how to embedd various servers to
emulate the remote environments which you may need to collaborate with.


# JDBC

Many Mule projects need to interact with an RDBMS at some point. Mule provides the JDBC transport for easy access
but many times, you'll want to avoid actually using a shared database. Sharing a database in a development
environment can cause several problems:

- Contention with other people (developers) or processes (CI servers)
- Ensuring the database is in the correct state for your tests (predictable data)
- Cleaning up your changes for the next set of tests

There are various ways of handling these problems. For instance, by using spring and it's transaction managed test
case, you can rollback any changes between tests but that's not an option we have in a Mule flow (not easily at least).
Another option would be to create a unique database for everyone who wishes to run the tests. This isn't really
realistic to manage long term.

A locally embeddded database which loads up fresh, predictable data upon test startup is the approach we're using here.

## H2 Database

The database we've selected is H2. Why H2?

- It's a simple but robust, embeddable database created by the original developer of the HSQL datbase.
- Has configurable database [compatability modes](http://www.h2database.com/html/grammar.html#set_mode) to emulate
non-ansi SQL grammar of other databases
- Clear and [throrough documentation](http://www.h2database.com/html/grammar.html)
- The ability to run SQL scripts on startup
- [CSV bulk loading](http://www.h2database.com/html/tutorial.html#csv) of data

Feel free to use