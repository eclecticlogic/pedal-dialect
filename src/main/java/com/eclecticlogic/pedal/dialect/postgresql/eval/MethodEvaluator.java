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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kabram on 4/6/17.
 */
public interface MethodEvaluator {

    void evaluate(Method method, EvaluatorChain chain);


    default void evaluate(Method method, Class<?> clz, List<CopyAttribute> attributes) {
        List<MethodEvaluator> evaluators = new ArrayList<>();
        evaluators.add(new IdentityIdEvaluator());
        evaluators.add(new SimpleColumnEvaluator());
        evaluators.add(new JoinColumnEvaluator());
        evaluators.add(new EmbeddedIdColumnEvaluator());
        evaluators.add(new EmbeddedColumnEvaluator());

        EvaluatorChain chain = new EvaluatorChain() {
            int index = 0;


            @Override
            public Class<?> getEntityClass() {
                return clz;
            }


            @Override
            public void add(CopyAttribute attribute) {
                attributes.add(attribute);
            }


            @Override
            public void doNext() {
                index++;
                if (index < evaluators.size()) {
                    evaluators.get(index).evaluate(method, this);
                }
            }
        };

        evaluators.get(0).evaluate(method, chain);
    }
}
