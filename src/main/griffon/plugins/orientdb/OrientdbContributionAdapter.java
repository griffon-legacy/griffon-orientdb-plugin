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

import java.util.Map;

/**
 * @author Andres Almiray
 */
public class OrientdbContributionAdapter implements OrientdbContributionHandler {
    private static final String DEFAULT = "default";

    private OrientdbProvider provider = DefaultOrientdbProvider.getInstance();

    public void setOrientdbProvider(OrientdbProvider provider) {
        this.provider = provider != null ? provider : DefaultOrientdbProvider.getInstance();
    }

    public OrientdbProvider getOrientdbProvider() {
        return provider;
    }

    public <R> R withOrientdb(Closure<R> closure) {
        return withOrientdb(DEFAULT, closure);
    }

    public <R> R withOrientdb(String databaseName, Closure<R> closure) {
        return provider.withOrientdb(databaseName, closure);
    }

    public <R> R withOrientdb(CallableWithArgs<R> callable) {
        return withOrientdb(DEFAULT, callable);
    }

    public <R> R withOrientdb(String databaseName, CallableWithArgs<R> callable) {
        return provider.withOrientdb(databaseName, callable);
    }
}