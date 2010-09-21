/* Maude ListTerm Code generator for RTMaude code generator

 Copyright (c) 2009-2010 The Regents of the University of California.
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
 * Generate a list RTMaude term (AU or ACU) for a data structure with an Iterator.
 *
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class ListTerm<T> {

    /** The delimiter for the list term representation, e.g., ",". */
    protected String delimiter;

    /** The term for the empty list. */
    protected String empty;

    /** The contents of the given list. */
    protected Iterator<T> iter;

    /**
     * Constructs a ListTerm object.
     *
     * @param empty      An empty term
     * @param delimiter  A delimiter for the given list term
     * @param target     An Iterable object which contains the elements
     */
    public ListTerm(String empty, String delimiter, Iterable<T> target) {
        this.iter = target.iterator();
        this.empty = empty;
        this.delimiter = delimiter;
    }

    /** Generates the term representation of the list using an empty term,
     * a delimiter, and an item member function.
     * @return The term representation of the list
     * @exception IllegalActionException
     */
    public String generateCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        String v = null;

        while (iter.hasNext() && (v = this.item(iter.next())) == null) {
            ;
        }
        if (v != null) {
            code.append(v);
        }

        while (iter.hasNext()) {
            v = this.item(iter.next());
            if (v != null) { // if null, it's screened out
                code.append(delimiter);
                code.append(v);
            }
        }
        if (code.length() > 0) {
            return code.toString();
        } else {
            return empty;
        }
    }

    /** Returns the term representation of the given element v.
     * By overriding this method, we can define any term representation
     * for elements in the list.
     *
     * @param v the element
     * @return the string representation of v
     * @exception IllegalActionException An overriding methods may generate this exception.
     */
    public String item(T v) throws IllegalActionException {
        return v.toString();
    }
}
