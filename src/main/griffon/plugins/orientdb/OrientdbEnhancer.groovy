/*
 * Copyright 2012-2013 the original author or authors.
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

import griffon.util.CallableWithArgs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.orientechnologies.orient.core.db.ODatabase
import com.orientechnologies.orient.core.record.impl.ODocument

/**
 * @author Andres Almiray
 */
final class OrientdbEnhancer {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(OrientdbEnhancer)

    private OrientdbEnhancer() {}
    
    static void enhance(MetaClass mc, OrientdbProvider provider = DefaultOrientdbProvider.instance) {
        if (LOG.debugEnabled) LOG.debug("Enhancing $mc with $provider")
        mc.withOrientdb = {Closure closure ->
            provider.withOrientdb(DEFAULT, closure)
        }
        mc.withOrientdb << {String databaseName, Closure closure ->
            provider.withOrientdb(databaseName, closure)
        }
        mc.withOrientdb << {CallableWithArgs callable ->
            provider.withOrientdb(DEFAULT, callable)
        }
        mc.withOrientdb << {String databaseName, CallableWithArgs callable ->
            provider.withOrientdb(databaseName, callable)
        }
    }

    static void enhanceOrient() {
        ExpandoMetaClass.enableGlobally()

        ODatabase.metaClass.withTransaction = { Closure closure ->
            delegate.begin()
            closure()
            delegate.commit()
        }

        ODocument.metaClass.propertyMissing = { String propertyName ->
            delegate.field(propertyName)
        }

        ODocument.metaClass.propertyMissing = { String propertyName, value ->
            delegate.field(propertyName, value)
        }
    }
}