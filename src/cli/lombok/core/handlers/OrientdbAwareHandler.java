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

import lombok.ast.Expression;
import lombok.ast.IMethod;
import lombok.ast.IType;

import static lombok.ast.AST.*;

/**
 * @author Andres Almiray
 */
public abstract class OrientdbAwareHandler<TYPE_TYPE extends IType<? extends IMethod<?, ?, ?, ?>, ?, ?, ?, ?, ?>> extends AbstractHandler<TYPE_TYPE> implements OrientdbAwareConstants {
    private Expression<?> defaultOrientdbProviderInstance() {
        return Call(Name(DEFAULT_ORIENTDB_PROVIDER_TYPE), "getInstance");
    }

    public void addOrientdbProviderField(final TYPE_TYPE type) {
        addField(type, ORIENTDB_PROVIDER_TYPE, ORIENTDB_PROVIDER_FIELD_NAME, defaultOrientdbProviderInstance());
    }

    public void addOrientdbProviderAccessors(final TYPE_TYPE type) {
        type.editor().injectMethod(
            MethodDecl(Type(VOID), METHOD_SET_ORIENTDB_PROVIDER)
                .makePublic()
                .withArgument(Arg(Type(ORIENTDB_PROVIDER_TYPE), PROVIDER))
                .withStatement(
                    If(Equal(Name(PROVIDER), Null()))
                        .Then(Block()
                            .withStatement(Assign(Field(ORIENTDB_PROVIDER_FIELD_NAME), defaultOrientdbProviderInstance())))
                        .Else(Block()
                            .withStatement(Assign(Field(ORIENTDB_PROVIDER_FIELD_NAME), Name(PROVIDER)))))
        );

        type.editor().injectMethod(
            MethodDecl(Type(ORIENTDB_PROVIDER_TYPE), METHOD_GET_ORIENTDB_PROVIDER)
                .makePublic()
                .withStatement(Return(Field(ORIENTDB_PROVIDER_FIELD_NAME)))
        );
    }

    public void addOrientdbContributionMethods(final TYPE_TYPE type) {
        delegateMethodsTo(type, METHODS, Field(ORIENTDB_PROVIDER_FIELD_NAME));
    }
}
