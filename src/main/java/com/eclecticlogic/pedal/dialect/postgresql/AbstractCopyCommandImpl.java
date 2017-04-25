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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

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

}
