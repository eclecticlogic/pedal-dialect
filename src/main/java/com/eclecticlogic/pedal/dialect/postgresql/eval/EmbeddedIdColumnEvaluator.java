/*
 * Copyright (c) 2017 Eclectic Logic LLC
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

package com.eclecticlogic.pedal.dialect.postgresql.eval;

import com.eclecticlogic.pedal.dialect.postgresql.CopyAttribute;

import javax.persistence.AttributeOverride;
import javax.persistence.EmbeddedId;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by kabram on 4/6/17.
 */
public class EmbeddedIdColumnEvaluator extends AbstractMethodEvaluator {

    @Override
    public void evaluate(Method method, EvaluatorChain chain) {
        if (method.isAnnotationPresent(EmbeddedId.class)) {
            Map<String, AttributeOverride> overrides = getAttributeOverrides(method);

            Class<?> embeddedClz = method.getReturnType();
            BeanInfo info = null;
            try {
                info = Introspector.getBeanInfo(embeddedClz);
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
            for (String propertyName : overrides.keySet()) {
                for (PropertyDescriptor propDesc : info.getPropertyDescriptors()) {
                    if (propDesc.getName().equals(propertyName)) {
                        CopyAttribute attribute = new CopyAttribute();
                        attribute.getMethods().add(method);
                        attribute.getMethods().add(propDesc.getReadMethod());
                        attribute.setColumnName(overrides.get(propertyName).column().name());
                        chain.add(attribute);
                        break;
                    }
                }
            }
        } else {
            chain.doNext();
        }
    }
}
