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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kabram on 4/6/17.
 */
public abstract class AbstractMethodEvaluator implements MethodEvaluator {


    protected String getPropertyName(Method method) {
        Class<?> clz = method.getDeclaringClass();
        String beanPropertyName = null;
        try {
            BeanInfo info = Introspector.getBeanInfo(clz);

            for (PropertyDescriptor propDesc : info.getPropertyDescriptors()) {
                if (method.equals(propDesc.getReadMethod())) {
                    beanPropertyName = propDesc.getName();
                    break;
                }
            }
            return beanPropertyName;
        } catch (IntrospectionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    protected String extractColumnName(Method method, Class<?> clz) {
        String beanPropertyName = getPropertyName(method);

        String columnName = null;
        if (clz.isAnnotationPresent(AttributeOverrides.class)) {
            for (AttributeOverride annotation : clz.getAnnotation(AttributeOverrides.class).value()) {
                if (annotation.name().equals(beanPropertyName)) {
                    columnName = annotation.column().name();
                    break;
                }
            }
        } else if (clz.isAnnotationPresent(AttributeOverride.class)) {
            AttributeOverride annotation = clz.getAnnotation(AttributeOverride.class);
            if (annotation.name().equals(beanPropertyName)) {
                columnName = annotation.column().name();
            }
        }
        return columnName == null ? method.getAnnotation(Column.class).name() : columnName;
    }


    protected Map<String, AttributeOverride> getAttributeOverrides(AccessibleObject object) {
        Map<String, AttributeOverride> overrides = new HashMap<>();
        if (object.isAnnotationPresent(AttributeOverrides.class)) {
            AttributeOverrides o = object.getAnnotation(AttributeOverrides.class);
            for (AttributeOverride override : o.value()) {
                overrides.put(override.name(), override);
            }
        }
        if (object.isAnnotationPresent(AttributeOverride.class)) {
            AttributeOverride override = object.getAnnotation(AttributeOverride.class);
            overrides.put(override.name(), override);
        }
        return overrides;
    }
}
