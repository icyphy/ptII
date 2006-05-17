/** A token that contains a FixPoint number.

 Copyright (c) 1998-2006 The Regents of the University of California.
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

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCL5AIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package ptolemy.data;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FixType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.FixPoint;
import ptolemy.math.FixPointQuantization;
import ptolemy.math.Overflow;
import ptolemy.math.Precision;
import ptolemy.math.Quantization;
import ptolemy.math.Rounding;

//////////////////////////////////////////////////////////////////////////
//// UnsizedFixToken

/**
 A token that contains an instance of FixPoint.  This token type
 exists solely so that types can be declared as UNSIZED_FIX through
 the parameter mechanism, since we don't represent types distinctly
 from tokens.  Generally speaking actors should process FixTokens,
 which properly report their precision.

 @author Steve Neuendorffer
 @see ptolemy.data.FixToken
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class UnsizedFixToken extends FixToken {

    /** Construct a token with integer 0.
     *  This method calls the {@link ptolemy.math.FixPoint#FixPoint(int)}
     *  constructor, so the precision and quantization are the what ever
     *  is defined for that constructor
     */
    public UnsizedFixToken() {
        super();
    }

    /** Construct an UnsizedFixToken with the supplied FixPoint value.
     *  @param value A FixPoint value.
     */
    public UnsizedFixToken(FixPoint value) {
        super(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the type of this token.
     *  @return BaseType.UNSIZED_FIX.
     */
    public Type getType() {
        return BaseType.UNSIZED_FIX;
    }
}
