
Orientdb support
----------------

Plugin page: [http://artifacts.griffon-framework.org/plugin/orientdb](http://artifacts.griffon-framework.org/plugin/orientdb)


The Orientdb plugin enables lightweight access to [Orientdb][1] databases.
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in
`$appdir/griffon-app/conf`:

 * OrientdbConfig.groovy - contains the database definitions.
 * BootstrapOrientdb.groovy - defines init/destroy hooks for data to be
   manipulated during app startup/shutdown.

A new dynamic method named `withOrientdb` will be injected into all controllers,
giving you access to a `com.orientechnologies.orient.object.db.OObjectDatabaseTx`
or `com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx` object,
with which you'll be able to make calls to the database. Remember to make all
database calls off the UI thread otherwise your application may appear unresponsive
when doing long computations inside the UI thread.

This method is aware of multiple databases. If no databaseName is specified
when calling it then the default database will be selected. Here are two example
usages, the first queries against the default database while the second queries
a database whose name has been configured as 'internal'

    package sample
    class SampleController {
        def queryAllDatabases = {
            withOrientdb { databaseName, database -> ... }
            withOrientdb('internal') { databaseName, database -> ... }
        }
    }

The following list enumerates all the variants of the injected method

 * `<R> R withOrientdb(Closure<R> stmts)`
 * `<R> R withOrientdb(CallableWithArgs<R> stmts)`
 * `<R> R withOrientdb(String databaseName, Closure<R> stmts)`
 * `<R> R withOrientdb(String databaseName, CallableWithArgs<R> stmts)`

These methods are also accessible to any component through the singleton
`griffon.plugins.orientdb.OrientdbConnector`. You can inject these methods to
non-artifacts via metaclasses. Simply grab hold of a particular metaclass and
call `OrientdbEnhancer.enhance(metaClassInstance, orientdbProviderInstance)`.

### API Enhancements

This plugin enhances the Orientdb API through meta classes in the following way

**com.orientechnologies.orient.core.db.ODatabase**

* `withTransaction(Closure closure)` - executes the closure within the boundaries of a transaction.

**com.orientechnologies.orient.core.record.impl.ODocument**

* `propertyMissing(String name)` - shortcut for calling `field(name)`.
* `propertyMissing(String name, value)` - shortcut for calling `field(name, value)`.

Configuration
-------------
### OrientdbAware AST Transformation

The preferred way to mark a class for method injection is by annotating it with
`@griffon.plugins.orientdb.OrientdbAware`. This transformation injects the
`griffon.plugins.orientdb.OrientdbContributionHandler` interface and default
behavior that fulfills the contract.

### Dynamic method injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.orientdb.injectInto = ['controller', 'service']

Dynamic method injection will be skipped for classes implementing
`griffon.plugins.orientdb.OrientdbContributionHandler`.

### Events

The following events will be triggered by this addon

 * OrientdbConnectStart[config, databaseName] - triggered before connecting to the database
 * OrientdbConnectEnd[databaseName, database] - triggered after connecting to the database
 * OrientdbDisconnectStart[config, databaseName, database] - triggered before disconnecting from the database
 * OrientdbDisconnectEnd[config, databaseName] - triggered after disconnecting from the database

### Multiple Databases

The config file `OrientdbConfig.groovy` defines a default database block. As the name
implies this is the database used by default, however you can configure named databases
by adding a new config block. For example connecting to a database whose name is 'internal'
can be done in this way

    databases {
        internal {
            url = 'local:/orient/databases/internal'
        }
    }

This block can be used inside the `environments()` block in the same way as the
default database block is used.

### Configuration Storage

The plugin will load and store the contents of `OrientdbConfig.groovy` inside the
application's configuration, under the `pluginConfig` namespace. You may retrieve
and/or update values using

    app.config.pluginConfig.orientdb

### Connect at Startup

The plugin will attempt a connection to the default database at startup. If this
behavior is not desired then specify the following configuration flag in
`Config.groovy`

    griffon.orientdb.connect.onstartup = false

### Example

A trivial sample application can be found at [https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/orientdb][2]

Testing
-------

Dynamic methods will not be automatically injected during unit testing, because
addons are simply not initialized for this kind of tests. However you can use
`OrientdbEnhancer.enhance(metaClassInstance, orientdbProviderInstance)` where
`orientdbProviderInstance` is of type `griffon.plugins.orientdb.OrientdbProvider`.
The contract for this interface looks like this

    public interface OrientdbProvider {
        <R> R withOrientdb(Closure<R> closure);
        <R> R withOrientdb(CallableWithArgs<R> callable);
        <R> R withOrientdb(String databaseName, Closure<R> closure);
        <R> R withOrientdb(String databaseName, CallableWithArgs<R> callable);
    }

It's up to you define how these methods need to be implemented for your tests.
For example, here's an implementation that never fails regardless of the
arguments it receives

    class MyOrientdbProvider implements OrientdbProvider {
        public <R> R withOrientdb(Closure<R> closure) { null }
        public <R> R withOrientdb(CallableWithArgs<R> callable) { null }
        public <R> R withOrientdb(String databaseName, Closure<R> closure) { null }
        public <R> R withOrientdb(String databaseName, CallableWithArgs<R> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            OrientdbEnhancer.enhance(service.metaClass, new MyOrientdbProvider())
            // exercise service methods
        }
    }

On the other hand, if the service is annotated with `@OrientdbAware` then usage
of `OrientdbEnhancer` should be avoided at all costs. Simply set `orientdbProviderInstance`
on the service instance directly, like so, first the service definition

    @griffon.plugins.orientdb.OrientdbAware
    class MyService {
        def serviceMethod() { ... }
    }

Next is the test

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            service.orientdbProvider = new MyOrientdbProvider()
            // exercise service methods
        }
    }

Tool Support
------------

### DSL Descriptors

This plugin provides DSL descriptors for Intellij IDEA and Eclipse (provided
you have the Groovy Eclipse plugin installed). These descriptors are found
inside the `griffon-orientdb-compile-x.y.z.jar`, with locations

 * dsdl/orientdb.dsld
 * gdsl/orientdb.gdsl

### Lombok Support

Rewriting Java AST in a similar fashion to Groovy AST transformations is
possible thanks to the [lombok][3] plugin.

#### JavaC

Support for this compiler is provided out-of-the-box by the command line tools.
There's no additional configuration required.

#### Eclipse

Follow the steps found in the [Lombok][3] plugin for setting up Eclipse up to
number 5.

 6. Go to the path where the `lombok.jar` was copied. This path is either found
    inside the Eclipse installation directory or in your local settings. Copy
    the following file from the project's working directory

         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/orientdb-<version>/dist/griffon-orientdb-compile-<version>.jar .

 6. Edit the launch script for Eclipse and tweak the boothclasspath entry so
    that includes the file you just copied

        -Xbootclasspath/a:lombok.jar:lombok-pg-<version>.jar:        griffon-lombok-compile-<version>.jar:griffon-orientdb-compile-<version>.jar

 7. Launch Eclipse once more. Eclipse should be able to provide content assist
    for Java classes annotated with `@OrientdbAware`.

#### NetBeans

Follow the instructions found in [Annotation Processors Support in the NetBeans
IDE, Part I: Using Project Lombok][4]. You may need to specify
`lombok.core.AnnotationProcessor` in the list of Annotation Processors.

NetBeans should be able to provide code suggestions on Java classes annotated
with `@OrientdbAware`.

#### Intellij IDEA

Follow the steps found in the [Lombok][3] plugin for setting up Intellij IDEA
up to number 5.

 6. Copy `griffon-orientdb-compile-<version>.jar` to the `lib` directory

         $ pwd
           $USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin
         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/orientdb-<version>/dist/griffon-orientdb-compile-<version>.jar lib

 7. Launch IntelliJ IDEA once more. Code completion should work now for Java
    classes annotated with `@OrientdbAware`.


[1]: http://code.google.com/p/orient
[2]: https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/orientdb
[3]: /plugin/lombok
[4]: http://netbeans.org/kb/docs/java/annotations-lombok.html

### Building

This project requires all of its dependencies be available from maven compatible repositories.
Some of these dependencies have not been pushed to the Maven Central Repository, however you
can obtain them from [lombok-dev-deps][lombok-dev-deps].

Follow the instructions found there to install the required dependencies into your local Maven
repository before attempting to build this plugin.

[lombok-dev-deps]: https://github.com/aalmiray/lombok-dev-deps