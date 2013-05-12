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

package griffon.plugins.orientdb;

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.ODatabase;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractOrientdbProvider implements OrientdbProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractOrientdbProvider.class);
    private static final String DEFAULT = "default";

    public <R> R withOrientdb(Closure<R> closure) {
        return withOrientdb(DEFAULT, closure);
    }

    public <R> R withOrientdb(String databaseName, Closure<R> closure) {
        if (isBlank(databaseName)) databaseName = DEFAULT;
        if (closure != null) {
            ODatabase database = getDatabase(databaseName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on databaseName '" + databaseName + "'");
            }
            try {
                return closure.call(databaseName, database);
            } finally {
                database.close();
            }
        }
        return null;
    }

    public <R> R withOrientdb(CallableWithArgs<R> callable) {
        return withOrientdb(DEFAULT, callable);
    }

    public <R> R withOrientdb(String databaseName, CallableWithArgs<R> callable) {
        if (isBlank(databaseName)) databaseName = DEFAULT;
        if (callable != null) {
            ODatabase database = getDatabase(databaseName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on databaseName '" + databaseName + "'");
            }
            try {
                callable.setArgs(new Object[]{databaseName, database});
                return callable.call();
            } finally {
                database.close();
            }
        }
        return null;
    }

    protected abstract ODatabase getDatabase(String databaseName);
}