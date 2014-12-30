/** 
 *  Copyright (c) 2011-2014 Eclectic Logic LLC. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Eclectic Logic LLC ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Eclectic Logic LLC.
 *
 **/
package com.eclecticlogic.pedal.provider;


/**
 * To allow java 7 interoperability.
 * @author kabram.
 *
 */
public interface Consumer<T> {
    
    
    void accept(T value);

}
