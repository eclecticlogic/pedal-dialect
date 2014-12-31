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
package com.eclecticlogic.pedal.dm;

import java.util.HashMap;
import java.util.Map;


/**
 * @author kabram.
 *
 */
public enum Status {

    ACTIVE("AC"), //
    INACTIVE("IC"), //
    ;
    
    private String code;


    private Status(String code) {
        this.code = code;
    }

    private static Map<String, Status> enumByCode = new HashMap<>();

    static {
        for (Status e : values()) {
            if (enumByCode.get(e.getCode()) != null) {
                throw new RuntimeException("Duplicate code found: " + e);
            }
            enumByCode.put(e.getCode(), e);
        }
    }


    public static Status forCode(String code) {
        return enumByCode.get(code);
    }


    public String getCode() {
        return code;
    }
}
