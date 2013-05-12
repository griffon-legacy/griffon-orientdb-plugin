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

package lombok.intellij.processor.clazz;

import com.intellij.psi.*;
import de.plushnikov.intellij.lombok.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.lombok.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.lombok.psi.LombokPsiElementFactory;
import de.plushnikov.intellij.lombok.util.PsiMethodUtil;
import de.plushnikov.intellij.lombok.util.PsiPrimitiveTypeFactory;
import griffon.plugins.orientdb.OrientdbAware;
import lombok.core.handlers.OrientdbAwareConstants;
import lombok.core.util.MethodDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Andres Almiray
 */
public class OrientdbAwareProcessor extends AbstractGriffonLombokClassProcessor implements OrientdbAwareConstants {
    private static final String ORIENTDB_PROVIDER_FIELD_INITIALIZER = DEFAULT_ORIENTDB_PROVIDER_TYPE + ".getInstance()";

    public OrientdbAwareProcessor() {
        super(OrientdbAware.class, PsiMethod.class);
    }

    protected <Psi extends PsiElement> void processIntern(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @NotNull List<Psi> target) {
        PsiElementFactory psiElementFactory = psiElementFactory(psiClass);
        PsiManager manager = psiClass.getContainingFile().getManager();

        PsiType psiProviderType = psiElementFactory.createTypeFromText(ORIENTDB_PROVIDER_TYPE, psiClass);
        LombokLightFieldBuilder providerField = LombokPsiElementFactory.getInstance().createLightField(manager, ORIENTDB_PROVIDER_FIELD_NAME, psiProviderType)
            .withContainingClass(psiClass)
            .withModifier(PsiModifier.PRIVATE)
            .withNavigationElement(psiAnnotation);
        PsiExpression initializer = psiElementFactory.createExpressionFromText(String.format(ORIENTDB_PROVIDER_FIELD_INITIALIZER), psiClass);
        providerField.setInitializer(initializer);

        LombokLightMethodBuilder method = LombokPsiElementFactory.getInstance().createLightMethod(psiClass.getManager(), METHOD_GET_ORIENTDB_PROVIDER)
            .withMethodReturnType(psiProviderType)
            .withContainingClass(psiClass)
            .withModifier(PsiModifier.PUBLIC)
            .withNavigationElement(psiAnnotation);
        target.add((Psi) method);

        method = LombokPsiElementFactory.getInstance().createLightMethod(psiClass.getManager(), METHOD_SET_ORIENTDB_PROVIDER)
            .withMethodReturnType(PsiPrimitiveTypeFactory.getInstance().getVoidType())
            .withContainingClass(psiClass)
            .withParameter(PROVIDER, psiProviderType)
            .withModifier(PsiModifier.PUBLIC)
            .withNavigationElement(psiAnnotation);
        target.add((Psi) method);

        for (MethodDescriptor methodDesc : METHODS) {
            target.add((Psi) PsiMethodUtil.createMethod(psiClass, methodDesc.signature, psiAnnotation));
        }
    }
}
