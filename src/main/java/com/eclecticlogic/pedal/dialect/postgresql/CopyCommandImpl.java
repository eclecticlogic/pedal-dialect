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

package com.eclecticlogic.pedal.dialect.postgresql;

import com.eclecticlogic.pedal.dialect.postgresql.eval.EvaluatorChain;
import com.eclecticlogic.pedal.dialect.postgresql.eval.MethodEvaluator;
import com.google.common.base.Stopwatch;
import javassist.*;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

/**
 * Created by kabram.
 */
public class CopyCommandImpl extends AbstractCopyCommandImpl {

    private static final String COPY_EXTRACTOR_PACKAGE = "com.eclecticlogic.pedal.dialect.postgresql.extractor";

    private ConcurrentHashMap<Class<? extends Serializable>, CopyExtractor<? extends Serializable>> extractorsByClass = new ConcurrentHashMap<>();
    // This is used to prevent linkage error due to concurrent creation of classes.
    private static AtomicInteger extractorNameSuffix = new AtomicInteger();
    private static final Logger logger = LoggerFactory.getLogger(CopyCommandImpl.class);


    @Override
    public <E extends Serializable> void insert(EntityManager entityManager, CopyList<E> entityList) {
        if (!entityList.isEmpty()) {
            _insert(entityManager, entityList);
        }
    }


    public <E extends Serializable> void _insert(EntityManager entityManager, CopyList<E> entityList) {
        Class<E> clz = (Class<E>) entityList.get(0).getClass();
        extractorsByClass.computeIfAbsent(clz, (cz) -> getExtractor(cz));

        CopyExtractor extractor = extractorsByClass.get(clz);
        providerAccessSpi.run(entityManager, connection -> {
            try {
                CopyManager copyManager = new CopyManager((BaseConnection) connectionAccessor.getRawConnection(connection));
                Encoding encoding = ((BaseConnection)connectionAccessor.getRawConnection(connection)).getEncoding();
                Stopwatch timer = Stopwatch.createStarted();
                CopyIn cp = copyManager.copyIn("copy " + getEntityName(entityList) + "(" + extractor.getFieldList() + ") from stdin");
                long records;
                try {
                    for (E e : entityList) {
                        byte[] buf = encoding.encode(extractor.getValueList(e));
                        cp.writeToCopy(buf, 0, buf.length);
                    }
                    records = cp.endCopy();
                } finally {
                    if (cp.isActive()) {
                        cp.cancelCopy();
                    }
                }
                assert records == entityList.size();
                logger.info("Wrote {} inserts in {}", records, timer.stop());// auto-selects units
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }


    protected List<CopyAttribute> getAttributesOfInterest(Class<? extends Serializable> clz) {
        List<CopyAttribute> attributes = new ArrayList<>();

        for (Method method : Arrays.stream(clz.getMethods()).filter(it -> it.getParameterCount() == 0).collect(toList())) {
            getMethodEvaluator().evaluate(method, clz, attributes);
        }
        return attributes;
    }


    protected MethodEvaluator getMethodEvaluator() {
        return new MethodEvaluator() {
            @Override
            public void evaluate(Method method, EvaluatorChain chain) {
                // noop
            }
        };
    }



    protected <E extends Serializable> CopyExtractor<E> getExtractor(Class<E> clz) {
        List<CopyAttribute> attributes = getAttributesOfInterest(clz);

        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(AbstractCopyExtractor.class));
        CtClass cc = pool.makeClass(COPY_EXTRACTOR_PACKAGE + clz.getSimpleName() + "$Extractor_" + extractorNameSuffix
                .incrementAndGet());
        try {
            cc.setSuperclass(pool.get(AbstractCopyExtractor.class.getName()));

            cc.addMethod(CtNewMethod.make(getFieldListBody(attributes), cc));
            cc.addMethod(CtNewMethod.make(getValueListBody(attributes, clz), cc));
        } catch (CannotCompileException | NotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            return (CopyExtractor<E>) cc.toClass().newInstance();
        } catch (CannotCompileException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    protected String getClassShell(String packageName, String clsName, String superName) {
        STGroup group = new STGroupFile("pedal/template/classShell.stg");
        ST st = group.getInstanceOf("classShell");
        st.add("pkgName", packageName);
        st.add("clsName", clsName);
        st.add("superName", superName);
        String s = st.render();
        logger.trace(s);
        return s;
    }


    protected String getFieldListBody(List<CopyAttribute> attributes) {
        STGroup group = new STGroupFile("pedal/template/methodGetFieldList.stg");
        ST st = group.getInstanceOf("methodBody");
        st.add("attributes", attributes);
        String s = st.render();
        logger.trace(s);
        return s;
    }


    protected String getValueListBody(List<CopyAttribute> attributes, Class<?> clz) {
        STGroup group = new STGroupFile("pedal/template/methodGetValueList.stg");
        ST st = group.getInstanceOf("methodBody");
        st.add("attributes", attributes);
        st.add("entityClass", clz);
        String s = st.render();
        logger.trace(s);
        return s;
    }
}
