/*
 * @(#)AbstractMethodError.java	1.13 98/09/21
 *
 * Copyright 1994-2000 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package java.lang;

/**
  * Thrown when an application tries to call an abstract method. 
 * Normally, this error is caught by the compiler; this error can 
 * only occur at run time if the definition of some class has 
 * incompatibly changed since the currently executing method was last
 * compiled. 
 *
 * @author  unascribed
 * @version 1.13, 09/21/98
 * @since   JDK1.0
 */
public
class AbstractMethodError extends IncompatibleClassChangeError {
    /**
     * Constructs an <code>AbstractMethodError</code> with no detail  message.
     */
    public AbstractMethodError() {
	super();
    }

    /**
     * Constructs an <code>AbstractMethodError</code> with the specified 
     * detail message. 
     *
     * @param   s   the detail message.
     */
    public AbstractMethodError(String s) {
	super(s);
    }
}
