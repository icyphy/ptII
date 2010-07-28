/* 
 * 
 * Copyright (c) 2010 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
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
 */
package ptolemy.data.ontologies;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class InfiniteConcept<T> extends Concept {

    // FIXME/Idea: What if creating an InfiniteConcept also requires a regular
    // concept in order to be created, and the infinite concept somehow just
    // borrows that concept's ontology/name/etc.
    // This would also mean that the graphical representation would need only
    // deal with the finite concepts; infinite concepts could start out as
    // having only a semantic difference.  Or maybe a flag property on a
    // standard concept saying that it was allowed to have infinite concepts,
    // or something like that.  That could even be checked in the infinite
    // concept's constructor, actually.
    public InfiniteConcept(Concept finiteConcept, T value)
            throws NameDuplicationException, IllegalActionException {
        super(finiteConcept.getOntology(), finiteConcept.getName());
        _value = value;
        _baseConcept = finiteConcept;
    }
    
    public T getParametrizedValue() {
        return _value;
    }
    
    private T _value;
    private Concept _baseConcept;

}
