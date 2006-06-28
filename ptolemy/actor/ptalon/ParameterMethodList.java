/* An list of parameter name/method pairs.

 Copyright (c) 1997-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package ptolemy.actor.ptalon;

import java.lang.reflect.Method;
import java.util.ArrayList;

import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
////ParameterMethodList

/**
A ParameterMethodList is a list of ordered pairs.  The first element
of each ordered pair is the name of a Parameter and the Method is a 
corresponding method for this parameter.  This will be used by
PtalonActor to store methods that will need to act when the 
corresponding parameter is given a value.
<p>

@author Adam Cataldo
@Pt.ProposedRating Red (acataldo)
@Pt.AcceptedRating Red (acataldo)
*/

public class ParameterMethodList {

    public ParameterMethodList() {
        _list = new ArrayList<PMPair>();
    }
    
    /**
     * Add a parameter name and corresponding method.
     * @param name The name.
     * @param method The method.
     */
    public void addPair(String name, Method method) {
        _list.add(new PMPair(name, method));
    }

    /**
     * Get a method corresonding to the given name.
     * If no such name exists, return null.
     * @param name The name.
     * @return The corresponding metod or null.
     */
    public Method getMethod(String name) {
        Method m = null;
        PMPair testPair;
        for (int i =0; i < _list.size(); i++) {
            testPair = _list.get(i);
            if (testPair.parameter.equals(name)) {
                m = testPair.method;
                break;
            }
        }
        return m;
    }
    
    /**
     * Get the size of this list.
     * @return The size of this list.
     */
    public int size() {
        return _list.size();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////
    
    /**
     * The list to store the elements.
     */
    private ArrayList<PMPair> _list;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private class                     ////
    
    /**
     * A PMPair is simply a parameter name and a method.
     */
    private class PMPair {
        /**
         * Create a new PMPair.
         * @param param The name.
         * @param meth The method.
         */
        public PMPair(String param, Method meth) {
            parameter = param;
            method = meth;
        }
        
        /**
         * The parameter name.
         */
        public String parameter;
        
        /**
         * The method.
         */
        public Method method;
    }
}
