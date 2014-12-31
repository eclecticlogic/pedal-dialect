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

/**
 * @author kabram.
 *
 */
public class PackagedCopyData {

    private String copySql;
    private String body;
    private int size;


    public String getBody() {
        return body;
    }


    public void setBody(String body) {
        this.body = body;
    }


    public String getCopySql() {
        return copySql;
    }


    public void setCopySql(String copySql) {
        this.copySql = copySql;
    }


    public int getSize() {
        return size;
    }


    public void setSize(int size) {
        this.size = size;
    }

}
