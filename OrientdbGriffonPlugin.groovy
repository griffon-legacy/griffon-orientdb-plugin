/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
class OrientdbGriffonPlugin {
    // the plugin version
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '0.9.5-SNAPSHOT > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-orientdb-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Orientdb support'
    String description = '''
The Orientdb plugin enables lightweight access to [Orientdb][1] databases.
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * OrientdbConfig.groovy - contains the database definitions.
 * BootstrapOrientdb.groovy - defines init/destroy hooks for data to be manipulated during app startup/shutdown.

A new dynamic method named `withOrientdb` will be injected into all controllers,
giving you access to either a `com.orientechnologies.orient.core.db.object.ODatabaseObjectTx` or 
`com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx` object, with which you'll be able
to make calls to the database. Remember to make all database calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.
This method is aware of multiple databases. If no databaseName is specified when calling
it then the default database will be selected. Here are two example usages, the first
queries against the default database while the second queries a database whose name has
been configured as 'internal'

	package sample
	class SampleController {
	    def queryAllDatabases = {
	        withOrientdb { databaseName, database -> ... }
	        withOrientdb('internal') { databaseName, database -> ... }
	    }
	}
	
This method is also accessible to any component through the singleton `griffon.plugins.orientdb.OrientdbConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`OrientdbEnhancer.enhance(metaClassInstance)`.

### API Enhancements

This plugin enhances the Orientdb API through meta classes in the following way

**com.orientechnologies.orient.core.db.ODatabase**

* `withTransaction(Closure closure)` - executes the closure within the boundaries of a transaction.

**com.orientechnologies.orient.core.record.impl.ODocument**

* `propertyMissing(String name)` - shortcut for calling `field(name)`.
* `propertyMissing(String name, value)` - shortcut for calling `field(name, value)`.

Configuration
-------------
### Dynamic method injection

The `withOrientdb()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.orientdb.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * OrientdbConnectStart[config, databaseName] - triggered before connecting to the database
 * OrientdbConnectEnd[databaseName, database] - triggered after connecting to the database
 * OrientdbDisconnectStart[config, databaseName, database] - triggered before disconnecting from the database
 * OrientdbDisconnectEnd[config, databaseName] - triggered after disconnecting from the database

### Multiple Stores

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

### Example

A trivial sample application can be found at [https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/orientdb][2]

Testing
-------
The `withOrientdb()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `OrientdbEnhancer.enhance(metaClassInstance, orientdbProviderInstance)` where 
`orientdbProviderInstance` is of type `griffon.plugins.orientdb.OrientdbProvider`. The contract for this interface looks like this

    public interface OrientdbProvider {
        Object withOrientdb(Closure closure);
        Object withOrientdb(String serverName, Closure closure);
        <T> T withOrientdb(CallableWithArgs<T> callable);
        <T> T withOrientdb(String serverName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyOrientdbProvider implements OrientdbProvider {
        Object withOrientdb(String serverName = 'default', Closure closure) { null }
        public <T> T withOrientdb(String serverName = 'default', CallableWithArgs<T> callable) { null }      
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            OrientdbEnhancer.enhance(service.metaClass, new MyOrientdbProvider())
            // exercise service methods
        }
    }


[1]: http://code.google.com/p/orient
[2]: https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/orientdb
'''
}
