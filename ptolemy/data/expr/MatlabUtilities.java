/* Utilities for accessing the Matlab engine.

Copyright (c) 2003 The Regents of the University of California
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)

*/
package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.kernel.util.*;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// MatlabUtilities
/** 
This class provides access to the Ptolemy Matlab interface in
ptolemy.matlab by using reflection.

@author Christopher Hylands, Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
@see ptolemy.data.expr.ParseTreeEvaluator
 */

public class MatlabUtilities {

    /** Evaluate a Matlab expression within a scope.
     *	The expression argument is of the form
     *  <em>matlab("expression(" + arg1 + "," arg2 + ")")</em>, where
     *  <em>arg1, + "," + arg2</em>is a list of arguments appearing in
     *  <em>"expression"</em>. Note that this form of invoking matlab
     *  is limited to returning only the first return value of a
     *  matlab function. If you need multiple return values, use the
     *  matlab {@link ptolemy.matlab.Expression} actor. If a
     *  "packageDirectories" Parameter is in the scope of this
     *  expression, its value is added to the matlab path while the
     *  expression is being executed (like {@link
     *  ptolemy.matlab.Expression}).
     *  @param expression The Matlab expression to be evaluated
     *  @param scope The scope to evaluate the expression within.
     *  @return The results of the evaluation
     */
    public static ptolemy.data.Token evaluate(String expression,
            ParserScope scope)
            throws IllegalActionException {

	try {
	    if (_engineClass == null) {
		_initialize();
	    }
	    ptolemy.data.Token result = null;



	    //MatlabEngineInterface matlabEngine =
	    //    MatlabEngineFactory.createEngine();

	    // Engine matlabEngine = new Engine();

	    Object matlabEngine = null;
	    try {
		matlabEngine = _engineClass.newInstance();
	    } catch (InstantiationException ex) {
		throw new IllegalActionException(null, ex,
                        "Failed to instantiate ptolemy.matlab.Engine");
	    }

	    //long[] engine = matlabEngine.open();

	    // Opening the matlab engine each time is very slow
	    _engine = (long []) _engineOpen
		.invoke(matlabEngine, new Object[0]);
	
	    try {
	    
		synchronized (
			      //matlabEngine.getSemaphore();
			      matlabEngine ) {

		    String addPathCommand = null;         // Assume none
		    ptolemy.data.Token previousPath = null;
		    ptolemy.data.Token packageDirectories = null;
		    if (scope != null) {
			packageDirectories =
			    scope.get("packageDirectories");
		    }
		    if (packageDirectories != null &&
                            packageDirectories instanceof StringToken) {
			StringTokenizer dirs = new
			    StringTokenizer
			    ((String)((StringToken)packageDirectories
				      ).stringValue(), ",");
			StringBuffer cellFormat = new StringBuffer(512);
			cellFormat.append("{");
			if (dirs.hasMoreTokens()) {
			    cellFormat.append
				("'" + UtilityFunctions
                                        .findFile(dirs.nextToken()) + "'");
			}
			while (dirs.hasMoreTokens()) {
			    cellFormat.append
				(",'" + UtilityFunctions
                                        .findFile(dirs.nextToken()) + "'");
			}
			cellFormat.append("}");
		    
			if (cellFormat.length() > 2) {
			    addPathCommand = "addedPath_=" +
				cellFormat.toString()
				+ ";addpath(addedPath_{:});";
			    //matlabEngine.evalString
			    //    (engine, "previousPath_=path");


			    //_engineEvalString.invoke(matlabEngine,
                            //        new Object[] {
                            //            _engine, "previousPath_=path"
                            //        });
                                

			    //previousPath = matlabEngine.get
			    //    (engine, "previousPath_");

			    //previousPath = (ptolemy.data.Token)
			    //	_engineGet.invoke(matlabEngine,
                            //        new Object[] {
                            //            _engine, "previousPath_"
                            //        });

			}
		    }
		    //matlabEngine.evalString
		    //    (engine, "clear variables;clear globals");

		    _engineEvalString.invoke(matlabEngine,
                            new Object[] {
                                _engine, "clear variables;clear globals"
                            });


		    if (addPathCommand != null) {
			// matlabEngine.evalString(engine, addPathCommand);
			_engineEvalString.invoke(matlabEngine,
                                new Object[] {
                                    _engine, addPathCommand
                                });
		    }
		    // Set scope variables
		    // This would be more efficient if the matlab engine
		    // understood the scope.
                    Set identifierSet = 
                        scope.identifierSet();
                    Iterator identifiers = 
                        identifierSet.iterator();
                    while(identifiers.hasNext()) {
                        String identifier = (String)identifiers.next();
                        // This was here...  don't understand why???
                        // if (var != packageDirectories)
                        
                        //matlabEngine.put
                        //    (engine, var.getName(), var.getToken());

                        _enginePut.invoke(matlabEngine,
                                new Object[] {
                                    _engine, identifier, scope.get(identifier)
                                });
                    }
		    //matlabEngine.evalString(engine,
		    //        "result__=" + expression);

		    _engineEvalString.invoke(matlabEngine,
                            new Object[] {
                                _engine, "result__=" + expression
                            });

		    //result = matlabEngine.get(engine, "result__");

		    result = (ptolemy.data.Token)
			_engineGet.invoke(matlabEngine,
                                new Object[] {
                                    _engine, "result__"
                                });
		}
	    }
	    finally {
		//matlabEngine.close(engine);
		_engineClose.invoke(matlabEngine,
				    new Object[] {
					_engine
				    });

	    }
	    return result;
	} catch (IllegalAccessException ex) {
	    throw new IllegalActionException(null, ex,
                    "Problem invoking a method on "
                    + "ptolemy.matlab.Engine");
	} catch (InvocationTargetException ex) {
	    throw new IllegalActionException(null, ex,
                    "Problem invoking a method of "
                    + "ptolemy.matlab.Engine");
	}

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ////

    // Initialize private variables.
    private static void _initialize() throws IllegalActionException {

	// These could be in the constructor, but since the evaluate()
	// method is a static method, we break out the functionality into
	// a separate method.

	// Use reflection so that we can compile without
	// ptolemy.matlab and we check to see if is present at runtime.

	try {
	    _engineClass = Class.forName("ptolemy.matlab.Engine");
	} catch (Throwable throwable) {
	    // UnsatsifiedLinkError is an Error, not an Exception, so
	    // we catch Throwable
	    throw new IllegalActionException(null, throwable,
                    "Failed to load ptolemy.matlab.Engine class");
	}

	try {
	    // Methods of ptolemy.matlab.Engine, in alphabetical order.

	    _engineClose = _engineClass.getMethod("close",
                    new Class[] {
                        long[].class
                    });


	    _engineEvalString = _engineClass.getMethod("evalString",
                    new Class[] {
                        long[].class,
                        String.class
                    });

	    _engineGet = _engineClass.getMethod("get",
                    new Class[] {
                        long[].class,
                        String.class
                    });

	    _engineOpen = _engineClass.getMethod("open", new Class[0]);

	    _enginePut = _engineClass.getMethod("put",
                    new Class[] {
                        long[].class,
                        String.class,
                        ptolemy.data.Token.class
                    });
	} catch (NoSuchMethodException ex) {
	    throw new IllegalActionException(null, ex,
                    "Problem finding a method of "
                    + "ptolemy.matlab.Engine");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    // The matlab engine pointer that is returned by matlab.Engine.open
    // We cache this value so that each time we evaluate a Matlab 
    // expression, we need not necessarily reopen the Engine.
    private static long [] _engine;

    // The class of ptolemy.matlab.Engine
    private static Class _engineClass;

    // Methods of ptolemy.matlab.Engine, in alphabetical order.

    // ptolemy.matlab.Engine.close();
    private static Method _engineClose;

    // ptolemy.matlab.Engine.evalString();
    private static Method _engineEvalString;

    // ptolemy.matlab.Engine.get();
    private static Method _engineGet;

    // ptolemy.matlab.Engine.open();
    private static Method _engineOpen;

    // ptolemy.matlab.Engine.put();
    private static Method _enginePut;
}


