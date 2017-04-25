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

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.lang.reflect.Method;

/**
 * Created by kabram on 4/6/17.
 */
public class IdentityIdEvaluator extends AbstractMethodEvaluator {

    @Override
    public void evaluate(Method method, EvaluatorChain chain) {
        if (method.isAnnotationPresent(Id.class) && method.isAnnotationPresent(GeneratedValue.class) && method.getAnnotation
                (GeneratedValue.class).strategy() == GenerationType.IDENTITY) {
            // Ignore identity generation-type id values as these are auto-generate.
        } else {
            chain.doNext();
        }
    }
}
