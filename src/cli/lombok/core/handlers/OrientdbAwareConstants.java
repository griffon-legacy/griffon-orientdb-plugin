/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lombok.core.handlers;

import lombok.core.BaseConstants;
import lombok.core.util.MethodDescriptor;

import static lombok.core.util.MethodDescriptor.args;
import static lombok.core.util.MethodDescriptor.type;
import static lombok.core.util.MethodDescriptor.typeParams;

/**
 * @author Andres Almiray
 */
public interface OrientdbAwareConstants extends BaseConstants {
    String ORIENTDB_PROVIDER_TYPE = "griffon.plugins.orientdb.OrientdbProvider";
    String DEFAULT_ORIENTDB_PROVIDER_TYPE = "griffon.plugins.orientdb.DefaultOrientdbProvider";
    String ORIENTDB_CONTRIBUTION_HANDLER_TYPE = "griffon.plugins.orientdb.OrientdbContributionHandler";
    String ORIENTDB_PROVIDER_FIELD_NAME = "this$orientdbProvider";
    String METHOD_GET_ORIENTDB_PROVIDER = "getOrientdbProvider";
    String METHOD_SET_ORIENTDB_PROVIDER = "setOrientdbProvider";
    String METHOD_WITH_ORIENTDB = "withOrientdb";
    String PROVIDER = "provider";

    MethodDescriptor[] METHODS = new MethodDescriptor[] {
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_ORIENTDB,
            args(type(GROOVY_LANG_CLOSURE, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_ORIENTDB,
            args(
                type(JAVA_LANG_STRING),
                type(GROOVY_LANG_CLOSURE, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_ORIENTDB,
            args(type(GRIFFON_UTIL_CALLABLEWITHARGS, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_ORIENTDB,
            args(
                type(JAVA_LANG_STRING),
                type(GRIFFON_UTIL_CALLABLEWITHARGS, R))
        )
    };
}
