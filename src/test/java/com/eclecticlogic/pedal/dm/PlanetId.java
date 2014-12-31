/**
 * Copyright (c) 2014 Eclectic Logic LLC
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

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

/**
 * @author kabram.
 *
 */
@SuppressWarnings("serial")
@Embeddable
public class PlanetId implements Serializable {

    private String name;
    private int position;


    public PlanetId() {
        super();
    }


    public PlanetId(String name, int position) {
        this.name = name;
        this.position = position;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public int getPosition() {
        return position;
    }


    public void setPosition(int position) {
        this.position = position;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getName(), getPosition());
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof PlanetId == false) {
            return false;
        } else {
            PlanetId typed = (PlanetId) obj;
            return Objects.equals(this.getName(), typed.getName()) && this.getPosition() == typed.getPosition();
        }
    }
}
