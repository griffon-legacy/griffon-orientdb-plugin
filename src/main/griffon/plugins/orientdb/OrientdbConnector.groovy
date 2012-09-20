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
import griffon.util.Environment
import griffon.util.Metadata
import griffon.util.CallableWithArgs
import griffon.util.ConfigUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
final class OrientdbConnector implements OrientdbProvider {
    private bootstrap

    private static final Logger LOG = LoggerFactory.getLogger(OrientdbConnector)

    Object withOrientdb(String databaseName = 'default', Closure closure) {
        OrientdbDatabaseHolder.instance.withOrientdb(databaseName, closure)
    }

    public <T> T withOrientdb(String databaseName = 'default', CallableWithArgs<T> callable) {
        return OrientdbDatabaseHolder.instance.withOrientdb(databaseName, callable)
    }

    // ======================================================

    ConfigObject createConfig(GriffonApplication app) {
        ConfigUtils.loadConfigWithI18n('OrientdbConfig')
    }

    private ConfigObject narrowConfig(ConfigObject config, String databaseName) {
        return databaseName == 'default' ? config.database : config.databases[databaseName]
    }

    ODatabase connect(GriffonApplication app, ConfigObject config, String databaseName = 'default') {
        if (OrientdbDatabaseHolder.instance.isDatabaseConnected(databaseName)) {
            return OrientdbDatabaseHolder.instance.getDatabase(databaseName)
        }

        config = narrowConfig(config, databaseName)
        app.event('OrientdbConnectStart', [config, databaseName])
        ODatabase database = startOrientdb(databaseName, config)
        OrientdbDatabaseHolder.instance.setDatabase(databaseName, config)
        bootstrap = app.class.classLoader.loadClass('BootstrapOrientdb').newInstance()
        bootstrap.metaClass.app = app
        withOrientdb(databaseName) { dsName, orient -> bootstrap.init(dsName, orient) }
        app.event('OrientdbConnectEnd', [databaseName, database])
        database
    }

    void disconnect(GriffonApplication app, ConfigObject config, String databaseName = 'default') {
        if (OrientdbDatabaseHolder.instance.isDatabaseConnected(databaseName)) {
            config = narrowConfig(config, databaseName)
            ODatabase database = OrientdbDatabaseHolder.instance.getDatabase(databaseName)
            app.event('OrientdbDisconnectStart', [config, databaseName, database])
            withOrientdb(databaseName) { dsName, orient -> bootstrap.destroy(dsName, orient) }
            stopOrientdb(config, database)
            app.event('OrientdbDisconnectEnd', [config, databaseName])
            OrientdbDatabaseHolder.instance.disconnectDatabase(databaseName)
        }
    }

    private ODatabase startOrientdb(String databaseName, ConfigObject config) {
        if (!config.url) {
            throw new IllegalArgumentException("Missing url: configuration for database '$database'")
        }
        config.username = config.username ?: 'guest'
        config.password = config.password ?: 'guest'

        ODatabase db = config.type == 'object' ? new OObjectDatabaseTx(config.url) : new ODatabaseDocumentTx(config.url)
        if (!db.exists()) db.create()
        db.close()
        db.open(config.username, config.password)
    }

    private void stopOrientdb(ConfigObject config, ODatabase database) {
        database.close()
    }
}
