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

import ptolemy.kernel.util.IllegalActionException;

import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// CachedMethod
/** Cache information about methods invoked from the Ptolemy expression
 parser (PtParser) and found among the registered function classes.<p>

Searching all the registered function classes for a method repetitively
every time when a Parameter containing a function expression is evaluated
became very expensive especially after introducing
polymorphicGetMethod() to correct for Java's reflection weaknesses.
This class provides a cache of function/method signatures already analyzed,
so that next time when the same method with the same signature is invoked
the search is replaced by fast hashed access to the cache.<p>

{@link # REAL} {@link #FUNCTION}s exist in classes registered with
PtParser.
{@link # REAL} {@link #METHOD}s exist in various class instances
(Tokens or other).
{@link #CONSTRUCTED} functions/methods are functions/methods
"constructed" by FindAndRunMethod using the argument dimension reduction
technique. {@link #MISSING} methods are the ones for which the search
has failed. The existence of either of these in the cache avoids
the expensive (function) search through all the registered
function classes.<p>

The cache is cleared by PtParser.registerFunctionClass() so that any
changes to the registered function classes cause the cache to be
re-generated.<p>

@author Zoltan Kemenczy, Research in Motion Limited.
@version $Id$
@since Ptolemy II 2.0
@see ptolemy.data.expr.ASTPtFunctionNode
@see ptolemy.data.expr.PtParser
*/

public class CachedMethod {

    /** Construct a new CachedMethod and compute its hashcode. */
    public CachedMethod(String methodName, Class[] argTypes,
            Method method, int type) {
        _methodName = methodName;
        _method = method;
        _type = type;
        _hashcode = methodName.hashCode();
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

    /** A "missing" method that is not real and cannot be constructed. */
    public static final int MISSING = 1;

    /** A method "constructed" by ASTPtFunctionNode by reducing argument
        array dimensions to reach a real method . */
    public static final int CONSTRUCTED = 2;

    /** A method/function that is "real" - found in class with the
        specified signature. */
    public static final int REAL = 4;

    /** A function (could end up real/constructed or missing). */
    public static final int FUNCTION = 8;

    /** A method (could end up real/constructed or missing). */
    public static final int METHOD = 16;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the argument represents the same CachedMethod as
        this. Required by Hashtable. */
    public boolean equals(Object arg) {
        boolean retval = true;
        if (!_methodName.equals(((CachedMethod)arg)._methodName))
            return false;
        if ((_type & (FUNCTION+METHOD)) !=
            (((CachedMethod)arg)._type & (FUNCTION+METHOD)))
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

    /** Find and run method specified by its methodName, argument types
     * and argument values by first checking in the method cache
     * followed by checking the first argument's class, followed by
     * checking all the function classes registered with PtParser. */
    public static Object findAndRunMethod
        (String methodName,
         Class[] argTypes,
         Object[] argValues,
         int type
         ) throws IllegalActionException {

	// First try to find the method in the cache...

	Object result = null;
        Method method = null;
        CachedMethod cachedMethod = get(methodName, argTypes, type);

        if (type == METHOD) {

            int num = argTypes.length;
            Class destTokenClass = argTypes[0];
            Class[] methodArgTypes = new Class[num - 1];
            Object[] methodArgValues = new Object[num - 1];
	    if (num == 1)
		methodArgValues = methodArgTypes = null;
            for (int i = 1; i < num; i++) {
                methodArgValues[i-1] = argValues[i];
                methodArgTypes[i-1] = argTypes[i];
            }
            if (cachedMethod != null ) {
                if (cachedMethod.getType() == REAL+METHOD) {
                    try {
                        method = cachedMethod.getMethod();
                        result = method.invoke(argValues[0], methodArgValues);
                    } catch (InvocationTargetException ex) {
                        // get the exception produced by the invoked
                        // function
                        ex.getTargetException().printStackTrace();
                        throw new IllegalActionException
                            ("Error invoking function " + methodName + "\n" +
                             ex.getTargetException().getMessage());
                    } catch (Exception ex)  {
                        throw new IllegalActionException(ex.getMessage());
                    }
                    return result;
                } else if (cachedMethod.getType() == MISSING+METHOD) {
                    return null;
                }
            } else {
                // Not found as a method. Search argument 0 class
                try {
                    method = argTypes[0].getMethod(methodName, methodArgTypes);
                    result = method.invoke(argValues[0], methodArgValues);
                } catch (NoSuchMethodException ex) {
                    // We haven't found the correct function.
                    // Try matching on argument type sub-classes.
                    try {
                        method = polymorphicGetMethod
                            (argTypes[0], methodName, methodArgTypes);
                        if (method != null) {
                            result = method.invoke
                                (argValues[0], methodArgValues);
                        }
                    } catch (SecurityException security) {
                        // If we are running under an Applet, then we
                        // may end up here if, for example, we try
                        // to invoke the non-existent quantize function on
                        // java.lang.Math.
                    } catch (InvocationTargetException exception) {
                        // get the exception produced by the invoked function
                        // exception.getTargetException().printStackTrace();
                        throw new IllegalActionException
                            ("Error invoking function " + methodName + "\n" +
                             exception.getTargetException().getMessage());
                    } catch (Exception exception)  {
                        throw new IllegalActionException
                            (null, exception, "Error invoking function " +
                                    methodName + " on " + argTypes[0]);
                    }
                } catch (InvocationTargetException ex) {
                    // get the exception produced by the invoked function
                    // ex.getTargetException().printStackTrace();
                    throw new IllegalActionException
                        ("Error invoking function " + methodName + "\n" +
                                ex.getTargetException().getMessage());
                } catch (Exception ex)  {
                    throw new IllegalActionException(ex.getMessage());
                }
            }

        } else { // type == FUNCTION
            if (cachedMethod != null ) {
                if (cachedMethod.getType() == REAL+FUNCTION) {
                    try {
                        method = cachedMethod.getMethod();
                        result = method.invoke(method.getDeclaringClass(), argValues);
                    } catch (InvocationTargetException ex) {
                        // get the exception produced by the invoked function
                        ex.getTargetException().printStackTrace();
                        throw new IllegalActionException
                            ("Error invoking function " + methodName + "\n" +
                             ex.getTargetException().getMessage());
                    } catch (Exception ex)  {
                        throw new IllegalActionException(ex.getMessage());
                    }
                    return result;
                } else if (cachedMethod.getType() == MISSING+FUNCTION) {
                    return null;
                }
            } else {
                // Not found as a method. Search the registered function classes
                Iterator allClasses = PtParser.getRegisteredClasses().iterator();
                while (allClasses.hasNext() && result == null) {
                    Class nextClass = (Class)allClasses.next();
                    //System.out.println("ASTPtFunctionNode: " + nextClass);
                    // First we look for the method, and if we get an exception,
                    // we ignore it and keep looking.
                    try {
                        method = nextClass.getMethod(methodName, argTypes);
                        result = method.invoke(nextClass, argValues);
                    } catch (NoSuchMethodException ex) {
                        // We haven't found the correct function.
                        // Try matching on argument type sub-classes.
                        try {
                            method = polymorphicGetMethod
                                (nextClass, methodName, argTypes);
                            if (method != null) {
                                result = method.invoke(nextClass, argValues);
                            }
                        } catch (SecurityException security) {
                            // If we are running under an Applet, then we
                            // may end up here if, for example, we try
                            // to invoke the non-existent quantize function on
                            // java.lang.Math.
                        } catch (InvocationTargetException exception) {
                            // get the exception produced by the invoked function
                            exception.getTargetException().printStackTrace();
                            throw new IllegalActionException
                                ("Error invoking function " + methodName + "\n" +
                                 exception.getTargetException().getMessage());
                        } catch (Exception exception)  {
                            throw new IllegalActionException
                                (null, exception, "Error invoking function " +
                                 methodName + " on " + nextClass);
                        }
                    } catch (InvocationTargetException ex) {
                        // get the exception produced by the invoked function
                        ex.getTargetException().printStackTrace();
                        throw new IllegalActionException
                            ("Error invoking function " + methodName + "\n" +
                             ex.getTargetException().getMessage());
                    } catch (Exception ex)  {
                        throw new IllegalActionException(ex.getMessage());
                    }
                }
            }
        }

	// If that failed, then try to reduce argument dimensions if possible
	// and try again (recursively)
	if (result == null) {
	    // Check if any arguments are of array type and, if any are, that they
	    // all have the same length.
	    boolean resIsArray = false;
	    int dim = 0;
	    Class[] nArgTypes = new Class[argTypes.length];
	    Object[] nArgValues = new Object[argValues.length];
	    for (int i = 0; i < argTypes.length; i++) {
		resIsArray |= argTypes[i].isArray();
		if (argTypes[i].isArray()) {
		    if (dim != 0 && Array.getLength(argValues[i]) != dim) {
                        // This argument does not have the same dimension as the
                        // first array argument encountered. Cannot recurse
                        // using this approach...
			resIsArray = false;
			break;
		    }
		    else {
                        // First array argument encounter
			dim = Array.getLength(argValues[i]);
			nArgTypes[i] = argTypes[i].getComponentType();
		    }
		}
		else {
		    nArgTypes[i] = argTypes[i];
		}
	    }
	    // If we found consistent array parameters, their dimensions have
	    // been reduced. Try method matching again
	    for (int d = 0; resIsArray && d < dim; d++) {
		for (int i = 0; i < argValues.length; i++) {
		    if (argTypes[i].isArray()) {
			nArgValues[i] = Array.get(argValues[i],d);
                        Class c = nArgValues[i].getClass();
                        // We have to use getClass() here because the above
                        // getComponentType() on Token[] returned by
                        // ArrayToken.arrayValue() does not reflect the true
                        // Token types. Now we have to check for
                        // primitive types and undo the damage...
                        if (nArgValues[i] instanceof Double) c = Double.TYPE;
                        if (nArgValues[i] instanceof Integer) c = Integer.TYPE;
                        if (nArgValues[i] instanceof Long) c = Long.TYPE;
                        nArgTypes[i] = c;
		    }
		    else {
			nArgValues[i] = argValues[i];
		    }
		}
		Object a = findAndRunMethod(methodName, nArgTypes, nArgValues, type);
		if (a == null)
		    break;
		Class c = a.getClass();
		if (a instanceof Double) c = Double.TYPE;
		if (a instanceof Integer) c = Integer.TYPE;
		if (a instanceof Long) c = Long.TYPE;
                if (result == null) {
                    result = Array.newInstance(c, dim);
                    Array.set(result,0,a);
                }
                else
                    Array.set(result,d,a);
	    }
            if (cachedMethod == null) {
                if (result != null) {
                    // Add newly found "constructed" method to the cache
                    // Note: all constructed Token[].method(Token[]) are
                    // now differentiated only by "method" name only
                    // (see above note on argTypes[i].getComponentType())
                    add(methodName, argTypes, method, type+CONSTRUCTED);
                } else {
                    // Add missing method to cache so we don't search for it
                    // again
                    add(methodName, argTypes, method, type+MISSING);
                }
            }
	} else if (cachedMethod == null) {
            // Add newly found real method to the cache
            add(methodName, argTypes, method, type+REAL);
        }
	return result;
    }

    /** Returns the CONSTRUCTED, MISSING, METHOD, FUNCTION type of this. */
    public int getType() {
        return _type;
    }

    /** Returns the method associated with this REAL CachedMethod, null
        otherwise. */
    public Method getMethod() {
        return _method;
    }

    /** Create and add a CachedMethod to the cache. */
    public static void add(String methodName, Class[] argTypes, Method method,
            int type) {
        CachedMethod cachedMethod = new CachedMethod
            (methodName, argTypes, method, type);
        _cachedMethods.put(cachedMethod, cachedMethod);
    }

    /** Clear the cache - restarts the search of registered classes for methods
        and the rebuilding of the cache. */
    public static void clear() {
        _cachedMethods = new Hashtable();
    }

    /** Return the CachedMethod that corresponds to methodName and argTypes if it
        had been cached previously. */
    public static CachedMethod get(String methodName, Class[] argTypes,
                                   int type) {
        CachedMethod key = new CachedMethod(methodName, argTypes, null, type);
        CachedMethod method = (CachedMethod)_cachedMethods.get(key);
        return method;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    // Regrettably, the getMethod() method in the java Class does not
    // support polymorphism.  In particular, it does not recognize a method
    // if the class you supply for an argument type is actually derived
    // from the class in the method signature.  So we have to reimplement
    // this here.  This method returns the first method that it finds that
    // has the specified name and can be invoked with the specified
    // argument types.  It is arguable that it should return the most
    // specific method that it finds, but it turns out that this is difficult
    // to define.  So it simply returns the first match.
    // It returns null if there is no match.
    protected static Method polymorphicGetMethod
        (Class library, String methodName, Class[] argTypes) {
        // NOTE: The Java docs do not explain the difference between
        // getMethods() and getDeclaredMethods(), so I'm guessing here...
        Method[] methods = library.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                Class[] arguments = methods[i].getParameterTypes();
                if (arguments.length != argTypes.length) continue;
                boolean match = true;
                for (int j = 0; j < arguments.length; j++) {
                    match = match && arguments[j].isAssignableFrom(argTypes[j]);
                }
                if (match) {
                    return methods[i];
                }
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _methodName;
    private Class[] _argTypes;
    private Method _method;
    private int _hashcode;
    private int _type;
    private static Hashtable _cachedMethods = new Hashtable();
}
