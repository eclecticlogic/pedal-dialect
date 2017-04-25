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

import com.eclecticlogic.pedal.connection.ConnectionAccessor;
import com.eclecticlogic.pedal.provider.ProviderAccessSpi;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kabram.
 */
public abstract class AbstractCopyCommandImpl implements CopyCommand {

    protected ConnectionAccessor connectionAccessor;
    protected ProviderAccessSpi providerAccessSpi;

    private static final Logger logger = LoggerFactory.getLogger(AbstractCopyCommandImpl.class);


    public void setConnectionAccessor(ConnectionAccessor connectionAccessor) {
        this.connectionAccessor = connectionAccessor;
    }


    public void setProviderAccessSpi(ProviderAccessSpi providerAccessSpi) {
        this.providerAccessSpi = providerAccessSpi;
    }


    protected <E extends Serializable> String getEntityName(CopyList<E> copyList) {
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











    @Override
    public void insert(EntityManager entityManager, PackagedCopyData data) {
        final StringReader reader = new StringReader(data.getBody());
        providerAccessSpi.run(entityManager, (connection) -> {
            try {
                CopyManager copyManager = new CopyManager((BaseConnection) connectionAccessor
                        .getRawConnection(connection));
                long t1 = System.currentTimeMillis();
                copyManager.copyIn(data.getCopySql(), reader);
                long elapsedTime = System.currentTimeMillis() - t1;
                logger.debug("Wrote {} inserts in {} seconds", data.getSize(), Math.round(elapsedTime / 10.0) / 100.0);
            } catch (Exception e) {
                logger.trace("Command passed: {}", data);
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }
}
