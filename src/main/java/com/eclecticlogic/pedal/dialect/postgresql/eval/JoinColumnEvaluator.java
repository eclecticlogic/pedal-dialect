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

import javax.persistence.JoinColumn;
import java.lang.reflect.Method;

/**
 * Created by kabram on 4/6/17.
 */
public class JoinColumnEvaluator extends AbstractMethodEvaluator {

    @Override
    public void evaluate(Method method, EvaluatorChain chain) {
        if (method.isAnnotationPresent(JoinColumn.class) && method.getDeclaredAnnotation(JoinColumn.class).insertable()) {
            CopyAttribute attribute = new CopyAttribute();
            attribute.getMethods().add(method);
            attribute.setColumnName(method.getAnnotation(JoinColumn.class).name());
            chain.add(attribute);
        } else {
            chain.doNext();
        }
    }
}
