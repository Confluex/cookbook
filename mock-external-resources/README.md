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

Feel free to use whatever embedded suites your needs (derby, hsql, etc.). This is just what we've chosen for
this example.


## Configuration

Our Mule flow declares some Spring beans which will help us manage our data source:

src/main/app/jdbc-config.xml

```xml
<mule xmlns="http://www.mulesoft.org/schema/mule/core"
      xmlns:vm="http://www.mulesoft.org/schema/mule/vm"
      xmlns:jdbc="http://www.mulesoft.org/schema/mule/jdbc"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:context="http://www.springframework.org/schema/context"
      xmlns:spring="http://www.springframework.org/schema/beans"
      xsi:schemaLocation="
http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
http://www.mulesoft.org/schema/mule/vm http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd
http://www.mulesoft.org/schema/mule/jdbc http://www.mulesoft.org/schema/mule/jdbc/current/mule-jdbc.xsd
http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd">

    <context:property-placeholder location="classpath:config.properties"/>

    <spring:beans>
        <spring:bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
            <!-- 1) externalize your properties -->
            <spring:property name="url" value="${jdbc.url}"/>
            <spring:property name="username" value="${jdbc.username}"/>
            <spring:property name="password" value="${jdbc.password}"/>
            <spring:property name="driverClassName" value="${jdbc.driver}"/>
            <spring:property name="initialSize" value="5"/>
            <spring:property name="testOnBorrow" value="true"/>
            <spring:property name="validationQuery" value="${jdbc.validationQuery}"/>
            <spring:property name="maxWait" value="60000"/>
            <spring:property name="maxActive" value="50"/>
            <spring:property name="removeAbandoned" value="true"/>
            <spring:property name="removeAbandonedTimeout" value="300"/>
            <spring:property name="logAbandoned" value="true"/>
        </spring:bean>

        <!-- 2) named queries externalized into files from the casspath -->
        <spring:bean id="userByUserNameQuery" class="org.apache.commons.io.IOUtils" factory-method="toString">
            <spring:constructor-arg type="java.io.InputStream">
                <spring:bean class="java.io.FileInputStream" destroy-method="close">
                    <spring:constructor-arg type="java.io.File" value="classpath:/jdbc/userByName.sql"/>
                </spring:bean>
            </spring:constructor-arg>
        </spring:bean>
    </spring:beans>

    <jdbc:connector name="defaultJdbcConnector" dataSource-ref="dataSource">
        <jdbc:query key="userByUserName" value-ref="userByUserNameQuery"/>
    </jdbc:connector>

    <vm:endpoint name="getUserInfo" path="user.info" exchange-pattern="request-response"/>
    <flow name="jdbc-test-flow">
        <inbound-endpoint ref="getUserInfo"/>
        <logger level="INFO" category="jdbc-test-flow" message="JDBC Parameters: #[payload]"/>
        <jdbc:outbound-endpoint queryKey="userByUserName" exchange-pattern="request-response"/>
        <logger level="INFO" category="jdbc-test-flow" message="JDBC Query Results: #[payload]"/>
    </flow>
</mule>
```

This is pretty normal configuration. There are a few interesting points to call out.

1. The properties are externalized and read from the __config.properties__.

In our __src/test/resources/config.properties__, we have settings for the embedded data source.

```ini
jdbc.url=jdbc:h2:mem:mule;INIT=RUNSCRIPT FROM 'classpath:/jdbc/ddl.sql';MODE=MYSQL
jdbc.username=sa
jdbc.password=
jdbc.driver=org.h2.Driver
jdbc.validationQuery=select 1
```

> Put your non-embedded config.properties somewhere on the classpath for the server (e.g. MULE\_HOME/conf or in src/main/resources)

Notice that we've made use of the h2 features to run a script: __ddl.sql__ and set the compatibility mode to __MYSQL__
_(to help us emulate our production environment better)_.

2. We've externalized our SQL query templates into a file. This isn't necessary but it's nicer than writting SQL inside
your XML file.

## DDL and Mock Data

In our config.properties (above), we told it to run a script: __src/test/resources/jdbc/ddl.sql__

```sql
CREATE TABLE IF NOT EXISTS user (
  user_name  VARCHAR(16) PRIMARY KEY,
  last_name  VARCHAR(32) NOT NULL,
  first_name VARCHAR(32) NOT NULL
)
AS SELECT
     user_name,
     last_name,
     first_name
   FROM CSVREAD('classpath:/jdbc/users.csv');
```

Again, we've made use of another nifty feature of h2: [CSV Bulk Loading](http://www.h2database.com/html/tutorial.html#csv).
This enables us to cleanly load a fresh set of tables and data upon each startup (since it's in-memory). If you have
more complex setup requirements, I'd take a look at:

- [Liquibase](http://www.liquibase.org/)
- [DBUnit](http://dbunit.sourceforge.net/)

## Dependencies

The only other thing we need is the database library itself. Since we're using a Maven build, just add it to the pom
with a scope of test:

```xml
<!-- mock: JDBC Server -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>1.3.168</version>
    <scope>test</scope>
</dependency>
```