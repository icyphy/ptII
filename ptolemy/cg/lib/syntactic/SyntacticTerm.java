/* Interface for terms in Syntactic expressions.

Copyright (c) 2010-2014 The Regents of the University of California.
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

package ptolemy.cg.lib.syntactic;

import java.util.List;

/** Represent terms in combinator expressions for Ptolemy models.
 *  <p>
 *  This interface should be inherited by any objects that will
 *  integrate into combinator expressions.
 *  <p>
 *  @author Chris Shaver
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (shaver)
 *  @Pt.AcceptedRating red
 *
 */
public interface SyntacticTerm {

    /** Return list of input Syntactic Ports to term.
     *  @return list of inputs.
     */
    public abstract List<SyntacticPort> getInputs();

    /** Return list of output Syntactic Ports to term.
     *  @return list of outputs.
     */
    public abstract List<SyntacticPort> getOutputs();

    /** Get number of inputs to term.
     *  @return number of inputs.
     */
    public abstract int sizeInputs();

    /** Get number of outputs to term.
     *  @return number of outputs.
     */
    public abstract int sizeOutputs();

    /** Get rank of term.
     *  @return rank of term.
     */
    public abstract SyntacticRank rank();

    /** Get index of given port or null.
     *  @param port to get the index of.
     *  @return index of given port or null
     */
    public abstract Integer inputIndex(SyntacticPort port);

    /** Get index of given port or null.
     *  @param port to get the index of.
     *  @return index of given port or null
     */
    public abstract Integer outputIndex(SyntacticPort port);

    /** Generate code for given term.
     *  @return generated code string.
     */
    public abstract String generateCode();

    /** Get the sort order of term.
     *  @return sort order of term.
     */
    public abstract int getOrder();

    /** Decide whether term can generate code representation.
     *  @return whether term can generate code.
     */
    public abstract boolean hasCode();

}
