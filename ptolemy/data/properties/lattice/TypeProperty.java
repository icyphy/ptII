/*
 * An interface for type property.
 * 
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2008-2009 The Regents of the University of California. All
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
package ptolemy.data.properties.lattice;

import ptolemy.data.Token;

/**
 * An interface for type property.
 * <p>This interface could be used to define a lattice for language such as
 * SystemC.
 * @author Man-Kit Leung, Thomas Mandl
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public interface TypeProperty {

    /** Return true if this element has minimum and maximum values.
     *  @return Return true if this element has minimum and maximum values.
     */
    public boolean hasMinMaxValue();

    /** Maximum value of a this type.
     *  @return The minimum value of this type.
     */
    public Token getMaxValue();

    /** Minimum value of a this type.
     *  @return The minimum value of this type.
     */
    public Token getMinValue();

    //    public boolean isInRange(Token token) throws IllegalActionException;
}
