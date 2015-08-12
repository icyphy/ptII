/* Utilities for accessing MoML.

 Copyright (c) 2009-2013 The Regents of the University of California
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


 */
package ptolemy.data.expr;

import java.lang.reflect.Method;

import ptolemy.data.ActorToken;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// MoMLUtilities

/** This class provides access to the Ptolemy MoML parser
 in ptolemy.moml by using reflection.

 @author Christopher Brooks.  Based on ParseMoML by Edward A. Lee. Based on MatlabUtilities by Steve Neuendorffer and Zoltan Kemenczy (Research in Motion Ltd.)
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ParseTreeEvaluator
 */
public class MoMLUtilities {
    /** Parse the string provided and return the result wrapped in a token.
     *  @param moml The MoML string.
     *  @return The result of parsing the MoML.
     *  @exception Exception If the MoML is invalid or the results is not an
     *   instance of Entity.
     */
    public static ActorToken parseMoML(String moml) throws Exception {
        // This code is tested in ptolemy/moml/test/parseMoML.tcl
        // because this code requires that ptolemy.moml be present
        try {
            if (_parserClass == null) {
                _initialize();
            }

            // This is not static so that we avoid memory leaks.
            Object parser = null;

            try {
                parser = _parserClass.newInstance();
            } catch (InstantiationException ex) {
                throw new IllegalActionException(null, ex,
                        "Failed to instantiate ptolemy.moml.MoMLParser");
            }

            // We synchronize here because MatlabUtilities is synchronized.
            synchronized (_parserClass) {
                _parserResetAll.invoke(parser, new Object[0]);

                NamedObj parseResult = (NamedObj) _parserParse.invoke(parser,
                        new Object[] { moml });
                return new ActorToken((Entity) parseResult);
            }
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Problem invoking a method on ptolemy.moml.MoMLParser");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Initialize private variables.
    private static void _initialize() throws IllegalActionException {
        // These could be in the constructor, but since the evaluate()
        // method is a static method, we break out the functionality into
        // a separate method.
        // Use reflection so that we can compile without
        // ptolemy.moml and we check to see if is present at runtime.
        try {
            _parserClass = Class.forName("ptolemy.moml.MoMLParser");
        } catch (Throwable throwable) {
            // UnsatsifiedLinkError is an Error, not an Exception, so
            // we catch Throwable
            throw new IllegalActionException(null, throwable,
                    "Failed to load ptolemy.moml.MoMLParser");
        }

        try {
            // Methods of ptolemy.moml.MoMLParser, in alphabetical order.
            _parserParse = _parserClass.getMethod("parse",
                    new Class[] { String.class });

            _parserResetAll = _parserClass.getMethod("resetAll", new Class[0]);
        } catch (NoSuchMethodException ex) {
            throw new IllegalActionException(null, ex,
                    "Problem finding a method of " + "ptolemy.moml.MoMLParser");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The class of ptolemy.moml.MoMLParser
    private static Class _parserClass;

    // Methods of ptolemy.moml.MoMLParser, in alphabetical order.

    // ptolemy.moml.MoMLParser.parse(String)
    private static Method _parserParse;

    // ptolemy.moml.MoMLParser.resetAll()
    private static Method _parserResetAll;
}
