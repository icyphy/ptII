/* Maude ListTerm Code generator for RTMaude code generator

 Copyright (c) 2009 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN AS IS BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.rtmaude.kernel.util;

import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ListTerm

/**
* Generate a list RTMaude term (AU or ACU) for an iterable data structure. 
*
* @author Kyungmin Bae
* @version $Id$
* @Pt.ProposedRating Red (kquine)
*
*/
public class ListTerm<T> {

    protected String delimiter;
    protected String empty;
    protected Iterator<T> iter;
    
    public ListTerm(String empty, String delimiter, Iterable<T> target) {
        this.iter = target.iterator();
        this.empty = empty;
        this.delimiter = delimiter;
    }
    public String generateCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        while (iter.hasNext()) {
            String v = this.item(iter.next());
            if (v != null) {        // if null, it's screened out
                code.append(v);  
                if (iter.hasNext()) code.append(delimiter);
            }
        }
        if (code.length() > 0)
            return code.toString();
        else
            return empty;
    }
    
    public String item(T v) throws IllegalActionException {
        return v.toString();
    }
}
