/*
 * A base class for monotonic function property constraints.
 * 
 * Copyright (c) 1998-2009 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 */
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.List;

import ptolemy.graph.InequalityTerm;

//////////////////////////////////////////////////////////////////////////
//// MonotonicFunction

/**
 * Monotonic functions are often used as part of the declaration of property
 * constraints. This base class makes it easy to do so. It is simply necessary
 * to implement the _getDependentTerms abstract methods.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public abstract class MonotonicFunction extends
        ptolemy.data.type.MonotonicFunction implements PropertyTerm {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the type variables in this inequality term. Derived classes should
     * implement this method to return an array of InequalityTerms that this
     * function depends on.
     * @return An array of InequalityTerm.
     */
    public final InequalityTerm[] getVariables() {
        List<InequalityTerm> terms = new ArrayList<InequalityTerm>();

        for (InequalityTerm term : _getDependentTerms()) {
            if (term.isSettable()) {
                terms.add(term);
            }
        }

        InequalityTerm[] array = new InequalityTerm[terms.size()];
        System.arraycopy(terms.toArray(), 0, array, 0, terms.size());

        return array;
    }

    /**
     * Return an array of constants contained in this term. If this term is a
     * variable, return an array of size zero; if this term is a constant,
     * return an array of size one that contains this constants; if this term is
     * a function, return an array containing all the constants in the function.
     * @return An array of InequalityTerms
     */
    public final InequalityTerm[] getConstants() {
        List<InequalityTerm> terms = new ArrayList<InequalityTerm>();

        for (InequalityTerm term : _getDependentTerms()) {
            if (!term.isSettable()) {
                terms.add(term);
            }
        }

        InequalityTerm[] array = new InequalityTerm[terms.size()];
        System.arraycopy(terms.toArray(), 0, array, 0, terms.size());

        return array;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /**
     * Return an array of terms depended by this monotonic function.
     * @return an array of terms depended by this monotonic function.
     */
    protected abstract InequalityTerm[] _getDependentTerms();
}
