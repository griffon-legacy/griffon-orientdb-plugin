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

import griffon.core.GriffonApplication
import griffon.util.Environment
import griffon.util.Metadata
import griffon.util.ConfigUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.orientechnologies.orient.core.db.ODatabase
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.object.db.OObjectDatabaseTx

/**
 * @author Andres Almiray
 */
@Singleton
final class OrientdbConnector {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(OrientdbConnector)
    private bootstrap

    ConfigObject createConfig(GriffonApplication app) {
        if (!app.config.pluginConfig.orientdb) {
            app.config.pluginConfig.orientdb = ConfigUtils.loadConfigWithI18n('OrientdbConfig')
        }
        app.config.pluginConfig.orientdb
    }

    private ConfigObject narrowConfig(ConfigObject config, String databaseName) {
        if (config.containsKey('database') && databaseName == DEFAULT) {
            return config.database
        } else if (config.containsKey('databases')) {
            return config.databases[databaseName]
        }
        return config
    }

    ODatabase connect(GriffonApplication app, ConfigObject config, String databaseName = DEFAULT) {
        if (DatabaseHolder.instance.isDatabaseConnected(databaseName)) {
            return DatabaseHolder.instance.getDatabase(databaseName)
        }

        config = narrowConfig(config, databaseName)
        app.event('OrientdbConnectStart', [config, databaseName])
        ODatabase database = startOrientdb(config, databaseName)
        DatabaseHolder.instance.setDatabase(databaseName, config)
        bootstrap = app.class.classLoader.loadClass('BootstrapOrientdb').newInstance()
        bootstrap.metaClass.app = app
        resolveOrientdbProvider(app).withOrientdb { dn, d -> bootstrap.init(dn, d) }
        app.event('OrientdbConnectEnd', [databaseName, database])
        database
    }

    void disconnect(GriffonApplication app, ConfigObject config, String databaseName = DEFAULT) {
        if (DatabaseHolder.instance.isDatabaseConnected(databaseName)) {
            config = narrowConfig(config, databaseName)
            ODatabase database = DatabaseHolder.instance.getDatabase(databaseName)
            app.event('OrientdbDisconnectStart', [config, databaseName, database])
            resolveOrientdbProvider(app).withOrientdb { dn, d -> bootstrap.destroy(dn, d) }
            stopOrientdb(config, database)
            app.event('OrientdbDisconnectEnd', [config, databaseName])
            DatabaseHolder.instance.disconnectDatabase(databaseName)
        }
    }

    OrientdbProvider resolveOrientdbProvider(GriffonApplication app) {
        def orientdbProvider = app.config.orientdbProvider
        if (orientdbProvider instanceof Class) {
            orientdbProvider = orientdbProvider.newInstance()
            app.config.orientdbProvider = orientdbProvider
        } else if (!orientdbProvider) {
            orientdbProvider = DefaultOrientdbProvider.instance
            app.config.orientdbProvider = orientdbProvider
        }
        orientdbProvider
    }

    private ODatabase startOrientdb(ConfigObject config, String databaseName) {
        if (!config.url) {
            throw new IllegalArgumentException("Missing url: configuration for database '$databaseName'")
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