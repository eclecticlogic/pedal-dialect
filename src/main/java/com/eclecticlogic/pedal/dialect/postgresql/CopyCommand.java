/**
 * Copyright (c) 2014-2015 Eclectic Logic LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.eclecticlogic.pedal.dialect.postgresql;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.eclecticlogic.pedal.connection.ConnectionAccessor;
import com.eclecticlogic.pedal.provider.Consumer;
import com.eclecticlogic.pedal.provider.ProviderAccessSpi;

/**
 * Allows collection of JPA entities to be written to the database using the Postgresql COPY command.
 * 
 * @author kabram.
 *
 */
public class CopyCommand {

    private ConnectionAccessor connectionAccessor;
    private ProviderAccessSpi providerAccessSpi;

    private ConcurrentHashMap<Class<? extends Serializable>, String> fieldNamesByClass = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class<? extends Serializable>, CopyExtractor<? extends Serializable>> extractorsByClass = new ConcurrentHashMap<>();
    // This is used to prevent linkage error due to concurrent creation of classes.
    private static AtomicInteger extractorNameSuffix = new AtomicInteger();

    private static Logger logger = LoggerFactory.getLogger(CopyCommand.class);


    public void setConnectionAccessor(ConnectionAccessor connectionAccessor) {
        this.connectionAccessor = connectionAccessor;
    }


    public void setProviderAccessSpi(ProviderAccessSpi providerAccessSpi) {
        this.providerAccessSpi = providerAccessSpi;
    }


    /**
     * @param lists Entities to be inserted using the Postgres COPY command.
     */
    public <E extends Serializable> void insert(EntityManager entityManager, CopyList<E> entityList) {
        if (!entityList.isEmpty()) {
            insert(entityManager, packageCopyData(entityList));
        }
    }


    /**
     * @param entityList
     * @return Packages up the entity list as standalone data that can be serialized and inserted at a later point.
     */
    @SuppressWarnings("unchecked")
    public <E extends Serializable> PackagedCopyData packageCopyData(CopyList<E> entityList) {
        Class<? extends Serializable> clz = entityList.get(0).getClass();
        setupFor(clz);
        String fieldNames = fieldNamesByClass.get(clz);
        CopyExtractor<E> extractor = (CopyExtractor<E>) extractorsByClass.get(clz);
        StringBuilder builder = new StringBuilder(1024 * entityList.size());
        for (E entity : entityList) {
            builder.append(extractor.getValueList(entity));
            builder.append("\n");
        }

        PackagedCopyData data = new PackagedCopyData();
        data.setBody(builder.toString());
        data.setSize(entityList.size());
        data.setCopySql("copy " + getEntityName(entityList) + "(" + fieldNames + ") from stdin");
        return data;
    }


    /**
     * Inserts packaged copy data using the COPY command.
     * @param entityManager
     * @param data
     */
    public void insert(EntityManager entityManager, final PackagedCopyData data) {
        final StringReader reader = new StringReader(data.getBody());
        providerAccessSpi.run(entityManager, new Consumer<Connection>() {

            @Override
            public void accept(Connection connection) {
                try {
                    CopyManager copyManager = new CopyManager((BaseConnection) connectionAccessor
                            .getRawConnection(connection));
                    long t1 = System.currentTimeMillis();
                    copyManager.copyIn(data.getCopySql(), reader);
                    long elapsedTime = System.currentTimeMillis() - t1;
                    logger.info("Wrote {} inserts in {} seconds", data.getSize(),
                            Math.round(elapsedTime / 10.0) / 100.0);
                } catch (Exception e) {
                    logger.trace("Command passed: {}", data);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

    }


    private void setupFor(Class<? extends Serializable> clz) {
        if (fieldNamesByClass.get(clz) == null) {
            List<String> fields = new ArrayList<>();
            List<Method> methods = new ArrayList<>();
            for (Method method : clz.getMethods()) {
                String columnName = null;
                if (method.isAnnotationPresent(Id.class) && method.isAnnotationPresent(GeneratedValue.class)
                        && method.getAnnotation(GeneratedValue.class).strategy() == GenerationType.IDENTITY) {
                    // Ignore pk with identity strategy.
                } else if (method.isAnnotationPresent(Column.class)) {
                    columnName = extractColumnName(method, clz);
                } else if (method.isAnnotationPresent(JoinColumn.class)
                        && method.getAnnotation(JoinColumn.class).insertable()) {
                    columnName = method.getAnnotation(JoinColumn.class).name();
                } else if (method.isAnnotationPresent(EmbeddedId.class)) {
                    // Handle Attribute override annotation ...
                    if (method.isAnnotationPresent(AttributeOverrides.class)) {
                        AttributeOverrides overrides = method.getAnnotation(AttributeOverrides.class);
                        for (AttributeOverride override : overrides.value()) {
                            fields.add(override.column().name());
                        }
                    }
                    methods.add(method);
                }
                if (columnName != null) {
                    // Certain one-to-on join situations can lead to multiple columns with the same column-name.
                    if (!fields.contains(columnName)) {
                        fields.add(columnName);
                        methods.add(method);
                    }
                } // end if annotation present
            }
            extractorsByClass.put(clz, getExtractor(clz, methods));
            StringBuilder builder = new StringBuilder();
            for (String  field : fields) {
                builder.append(",").append(field);
            }
            fieldNamesByClass.put(clz, builder.substring(1));
        }
    }


    private String extractColumnName(Method method, Class<? extends Serializable> clz) {
        String beanPropertyName = null;
        try {
            BeanInfo info;

            info = Introspector.getBeanInfo(clz);

            for (PropertyDescriptor propDesc : info.getPropertyDescriptors()) {
                if (method.equals(propDesc.getReadMethod())) {
                    beanPropertyName = propDesc.getName();
                    break;
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

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


    /**
     * Note: This method is now "ugly" and needs serious refactoring.
     * @param clz
     * @param fieldMethods
     * @return Create a class to generate the copy row strings.
     */
    @SuppressWarnings({ "unchecked" })
    private <E extends Serializable> CopyExtractor<E> getExtractor(Class<E> clz, List<Method> fieldMethods) {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass("com.eclecticlogic.pedal.dialect.postgresql." + clz.getSimpleName()
                + "$CopyExtractor_" + extractorNameSuffix.incrementAndGet());

        StringBuilder methodBody = new StringBuilder();
        try {
            cc.addInterface(pool.getCtClass(CopyExtractor.class.getName()));

            methodBody.append("public String getValueList(Object entity) {\n");
            methodBody.append("try {\n");
            methodBody.append("StringBuilder builder = new StringBuilder();\n");
            methodBody.append(clz.getName() + " typed = (" + clz.getName() + ")entity;\n");
            for (int i = 0; i < fieldMethods.size(); i++) {
                Method method = fieldMethods.get(i);
                if (method.isAnnotationPresent(CopyConverter.class)) {
                    try {
                        Class<?> helper = method.getAnnotation(CopyConverter.class).value();
                        methodBody.append(helper.getName() + " c" + i + " = (" + helper.getName() + ")"
                                + helper.getName() + ".class.newInstance();\n");
                        methodBody.append("builder.append(c" + i + ".convert(typed." + method.getName() + "()));\n");
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                } else if (method.getReturnType().isPrimitive()) {
                    methodBody.append("builder.append(typed." + method.getName() + "());\n");
                } else {
                    methodBody.append(method.getReturnType().getName() + " v" + i + " = typed." + method.getName()
                            + "();\n");
                    if (method.isAnnotationPresent(EmbeddedId.class)) {
                        // Embedded id
                        if (method.isAnnotationPresent(AttributeOverrides.class)) {
                            AttributeOverrides overrides = method.getAnnotation(AttributeOverrides.class);
                            for (int j = 0; j < overrides.value().length; j++) {
                                AttributeOverride override = overrides.value()[j];
                                methodBody.append("if (v" + i + " == null) {builder.append(\"\\\\N\");}\n");
                                methodBody.append("else {\n");
                                Method idMethod = BeanUtils.getPropertyDescriptor(method.getReturnType(),
                                        override.name()).getReadMethod();
                                methodBody.append("builder.append(v" + i + "." + idMethod.getName() + "());\n");
                                methodBody.append("}\n");
                                if (j != overrides.value().length - 1) {
                                    methodBody.append("builder.append(\"\\t\");\n");
                                }
                            }
                        }
                    } else {
                        methodBody.append("if (v" + i + " == null) {builder.append(\"\\\\N\");}\n");
                        methodBody.append("else {\n");

                        if (method.isAnnotationPresent(CopyAsBitString.class)) {
                            methodBody.append("java.util.Iterator it" + i + " = typed." + method.getName()
                                    + "().iterator();\n");
                            methodBody.append("while (it" + i + ".hasNext()) {\n");
                            methodBody.append("Boolean b = (Boolean)it" + i + ".next();\n");
                            methodBody.append("builder.append(b.booleanValue() ? \"0\" : \"1\");\n");
                            methodBody.append("}\n");
                        } else if (Collection.class.isAssignableFrom(method.getReturnType())
                                && method.isAnnotationPresent(Convert.class) == false) {
                            // Postgreql array type.
                            if (method.isAnnotationPresent(CopyEmptyAsNull.class)) {
                                methodBody.append("if (typed." + method.getName() + "().isEmpty()) {\n");
                                methodBody.append("builder.append(\"\\\\N\");\n");
                                methodBody.append("} else {\n");
                                collectionExtractor(methodBody, i, method);
                                methodBody.append("}\n");
                            } else {
                                collectionExtractor(methodBody, i, method);
                            }
                        } else if (method.isAnnotationPresent(Convert.class)) {
                            Class<?> converterClass = method.getAnnotation(Convert.class).converter();
                            methodBody.append(converterClass.getName() + " c" + i + " = (" + converterClass.getName()
                                    + ")" + converterClass.getName() + ".class.newInstance();\n");
                            methodBody.append("builder.append(c" + i + ".convertToDatabaseColumn(v" + i + "));\n");
                        } else if (method.isAnnotationPresent(JoinColumn.class)) {
                            // We need to get the id of the joined object.
                            for (Method method2 : method.getReturnType().getMethods()) {
                                if (method2.isAnnotationPresent(Id.class)) {
                                    methodBody.append("builder.append(v" + i + "." + method2.getName() + "());\n");
                                }
                            }
                        } else {
                            methodBody.append("builder.append(v" + i + ");\n");
                        }
                        methodBody.append("}\n");
                    }
                }
                if (i != fieldMethods.size() - 1) {
                    methodBody.append("builder.append(\"\\t\");\n");
                }
            }
            methodBody.append("return builder.toString();\n");
            methodBody.append("} catch (Exception e) { throw new RuntimeException(e); } \n");
            methodBody.append("}\n");
            logger.trace(methodBody.toString());
            cc.addMethod(CtNewMethod.make(methodBody.toString(), cc));
        } catch (NotFoundException | CannotCompileException e) {
            logger.error(e.getMessage(), "Compiled body is:\n" + methodBody.toString());
            throw new RuntimeException(e.getMessage(), e);
        }

        try {
            return (CopyExtractor<E>) cc.toClass().newInstance();
        } catch (InstantiationException | IllegalAccessException | CannotCompileException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private void collectionExtractor(StringBuilder methodBody, int i, Method method) {
        methodBody.append("java.util.Iterator it" + i + " = typed." + method.getName() + "().iterator();\n");
        methodBody.append("StringBuilder array" + i + " = new StringBuilder();\n");
        methodBody.append("while (it" + i + ".hasNext()) {\n");
        methodBody.append("Object o = it" + i + ".next();\n");
        methodBody.append("array" + i + ".append(\",\").append(o);\n");
        methodBody.append("}\n");
        methodBody.append("String arrayStr" + i + " = array" + i + ".length() == 0 ? \"\" : array" + i
                + ".substring(1);\n");
        methodBody.append("builder.append(\"{\").append(arrayStr" + i + ").append(\"}\");\n");
    }


    private <E extends Serializable> String getEntityName(CopyList<E> copyList) {
        String alternateName = copyList.getAlternateTableName();
        if (alternateName == null || alternateName.trim().length() == 0) {
            return providerAccessSpi.getTableName(copyList.get(0).getClass());
        } else {
            String schemaName = providerAccessSpi.getSchemaName();
            if (schemaName == null || schemaName.trim().length() == 0) {
                return copyList.getAlternateTableName();
            } else {
                return schemaName + "." + copyList.getAlternateTableName();
            }
        }
    }

}
