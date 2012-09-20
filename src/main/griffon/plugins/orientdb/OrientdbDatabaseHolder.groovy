/*
 * Copyright 2012 the original author or authors.
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
import griffon.util.CallableWithArgs

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Andres Almiray
 */
@Singleton
class OrientdbDatabaseHolder implements OrientdbProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OrientdbDatabaseHolder)
    private static final Object[] LOCK = new Object[0]
    private final Map<String, ConfigObject> configurations = [:]

    String[] getDatabaseNames() {
        List<String> databaseNames = [].addAll(configurations.keySet())
        databaseNames.toArray(new String[databaseNames.size()])
    }

    ODatabase getDatabase(String databaseName = 'default') {
        if (isBlank(databaseName)) databaseName = 'default'
        acquireDatabase(retrieveConfiguration(databaseName))
    }

    void setDatabase(String databaseName = 'default', ConfigObject config) {
        if (isBlank(databaseName)) databaseName = 'default'
        storeConfiguration(databaseName, config)
    }

    Object withOrientdb(String databaseName = 'default', Closure closure) {
        ODatabase database = fetchDatabase(databaseName)
        if (LOG.debugEnabled) LOG.debug("Executing statement on database '$databaseName'")
        try {
            return closure(databaseName, database)
        } finally {
            database.close()
        }
    }

    public <T> T withOrientdb(String databaseName = 'default', CallableWithArgs<T> callable) {
        ODatabase database = fetchDatabase(databaseName)
        if (LOG.debugEnabled) LOG.debug("Executing statement on database '$databaseName'")
        callable.args = [databaseName, database] as Object[]
        try {
            return callable.call()
        } finally {
            database.close()
        }
    }

    boolean isDatabaseConnected(String databaseName) {
        if (isBlank(databaseName)) databaseName = 'default'
        retrieveConfiguration(databaseName) != null
    }

    void disconnectDatabase(String databaseName) {
        if (isBlank(databaseName)) databaseName = 'default'
        storeConfiguration(databaseName, null)
    }

    private ODatabase fetchDatabase(String databaseName) {
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
