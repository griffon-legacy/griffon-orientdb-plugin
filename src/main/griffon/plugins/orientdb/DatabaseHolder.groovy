/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.orientdb

import com.orientechnologies.orient.core.db.ODatabase
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.object.db.OObjectDatabaseTx

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Andres Almiray
 */
class DatabaseHolder {
    private static final String DEFAULT = 'default'
    private static final Object[] LOCK = new Object[0]
    private final Map<String, ConfigObject> configurations = [:]

    private static final DatabaseHolder INSTANCE

    static {
        INSTANCE = new DatabaseHolder()
    }

    static DatabaseHolder getInstance() {
        INSTANCE
    }

    private DatabaseHolder() {}

    String[] getDatabaseNames() {
        List<String> databaseNames = new ArrayList().addAll(configurations.keySet())
        databaseNames.toArray(new String[databaseNames.size()])
    }

    ODatabase getDatabase(String databaseName = DEFAULT) {
        if (isBlank(databaseName)) databaseName = DEFAULT
        acquireDatabase(retrieveConfiguration(databaseName))
    }

    void setDatabase(String databaseName = DEFAULT, ConfigObject config) {
        if (isBlank(databaseName)) databaseName = DEFAULT
        storeConfiguration(databaseName, config)
    }

    boolean isDatabaseConnected(String databaseName) {
        if (isBlank(databaseName)) databaseName = DEFAULT
        retrieveConfiguration(databaseName) != null
    }
    
    void disconnectDatabase(String databaseName) {
        if (isBlank(databaseName)) databaseName = DEFAULT
        storeConfiguration(databaseName, null)
    }

    ODatabase fetchDatabase(String databaseName) {
        if (isBlank(databaseName)) databaseName = 'default'
        ODatabase database = acquireDatabase(retrieveConfiguration(databaseName))
        if (database == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = OrientdbConnector.instance.createConfig(app)
            database = OrientdbConnector.instance.connect(app, config, databaseName)
        }

        if (database == null) {
            throw new IllegalArgumentException("No such orientdb database configuration for name $databaseName")
        }
        database
    }

    private ConfigObject retrieveConfiguration(String databaseName) {
        synchronized (LOCK) {
            configurations[databaseName]
        }
    }

    private void storeConfiguration(String databaseName, ConfigObject config) {
        synchronized (LOCK) {
            configurations[databaseName] = config
        }
    }

    private ODatabase acquireDatabase(ConfigObject config) {
        if (config == null) return null
        ODatabase db = config.type == 'object' ? new OObjectDatabaseTx(config.url) : new ODatabaseDocumentTx(config.url)
        if (!db.exists()) db.create()
        db.close()
        db.open(config.username, config.password)
    }
}