/* CachedMethod provides a fast hashtable based lookup for methods

 Copyright (c) 1998-2002 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (zkemenczy@rim.net)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 2002
*/
package ptolemy.data.expr;

import java.lang.reflect.Method;
import java.util.Hashtable;

//////////////////////////////////////////////////////////////////////////
//// CachedMethod
/** Cache information about methods invoked from the Ptolemy expression
 parser (PtParser) and found among the registered function classes.<p>

Searching all the registered function classes for a method repetitively
every time when a Parameter containing a function expression is evaluated
became very expensive especially after introducing
_polymorphicGetMethod() to correct for Java's reflection weaknesses.
This class provides a cache of function/method signatures already analyzed,
so that next time when the same method with the same signature is invoked
the search is replaced by fast hashed access to the cache.<p>

The cache is cleared by PtParser.registerFunctionClass() so that any
changes to the registered function classes cause the cache to be
re-generated.

@author Zoltan Kemenczy, Research in Motion Limited.
@version $Id$
@see ptolemy.data.expr.ASTPtFunctionNode
@see ptolemy.data.expr.PtParser
*/

public class CachedMethod {

    /** Construct a new CachedMethod and compute its hashcode. */
    public CachedMethod(String funcName, Class[] argTypes,
            Method method, int type) {
        _funcName = funcName;
        _method = method;
        _type = type;
        _hashcode = funcName.hashCode();
        _argTypes = new Class[argTypes.length];
        // We copy the argument types since the argTypes
        // array reference may be modified by ASTPtFunctionNode
        // after it is created.
        for (int i = 0; i < argTypes.length; i++) {
            _argTypes[i] = argTypes[i];
            _hashcode += argTypes[i].hashCode();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A real method that exists in a class registered with PtParser. */
    public static final int REAL = 0;

    /** A method "constructed" by ASTPtFunctionNode by reducing argument
        array dimensions to reach a real method . */
    public static final int CONSTRUCTED = 1;

    /** A "missing" method that is not real and cannot be constructed. */
    public static final int MISSING = 2;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the argument represents the same CachedMethod as
        this. Required by Hashtable. */
    public boolean equals(Object arg) {
        boolean retval = true;
        if (!_funcName.equals(((CachedMethod)arg)._funcName))
            return false;
        if (_argTypes.length != ((CachedMethod)arg)._argTypes.length)
            return false;
        for (int i = 0; i < _argTypes.length; i++) {
            if (!(_argTypes[i] == ((CachedMethod)arg)._argTypes[i]))
                return false;
        }
        return true;
    }

    /** Return the hashcode calculated when this was constructed. */
    public int hashCode() {
        return _hashcode;
    }

    /** Returns the REAL, CONSTRUCTED, or MISSING type of this. */
    public int getType() {
        return _type;
    }

    /** Returns the method associated with this REAL CachedMethod, null
        otherwise. */
    public Method getMethod() {
        return _method;
    }

    /** Create and add a CachedMethod to the cache. */
    public static void add(String funcName, Class[] argTypes, Method method,
            int type) {
        CachedMethod cachedMethod = new CachedMethod
            (funcName, argTypes, method, type);
        _cachedMethods.put(cachedMethod, cachedMethod);
    }

    /** Clear the cache - restarts the search of registered classes for methods
        and the rebuilding of the cache. */
    public static void clear() {
        _cachedMethods = new Hashtable();
    }

    /** Return the CachedMethod that corresponds to funcName and argTypes if it
        had been cached previously. */
    public static CachedMethod get(String funcName, Class[] argTypes) {
        CachedMethod key = new CachedMethod(funcName, argTypes, null, -1);
        CachedMethod method = null;
        method = (CachedMethod)_cachedMethods.get(key);
        return method;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private String _funcName;
    private Class[] _argTypes;
    private Method _method;
    private int _hashcode;
    private int _type;
    private static Hashtable _cachedMethods = new Hashtable();
}
