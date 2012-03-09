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

import griffon.core.GriffonApplication
import griffon.plugins.orientdb.OrientdbConnector
import griffon.plugins.orientdb.OrientdbEnhancer

import com.orientechnologies.orient.core.Orient
import com.orientechnologies.orient.client.remote.OEngineRemote

/**
 * @author Andres Almiray
 */
class OrientdbGriffonAddon {
    void addonInit(GriffonApplication app) {
        OrientdbEnhancer.enhanceOrient()
        Orient.instance().registerEngine(new OEngineRemote())
        ConfigObject config = OrientdbConnector.instance.createConfig(app)
        OrientdbConnector.instance.connect(app, config)
    }

    void addonPostInit(GriffonApplication app) {
        def types = app.config.griffon?.orientdb?.injectInto ?: ['controller']
        for(String type : types) {
            for(GriffonClass gc : app.artifactManager.getClassesOfType(type)) {
                OrientdbEnhancer.enhance(gc.metaClass)
            }
        }
    }

    def events = [
        ShutdownStart: { app ->
            ConfigObject config = OrientdbConnector.instance.createConfig(app)
            OrientdbConnector.instance.disconnect(app, config)
        }
    ]
}
