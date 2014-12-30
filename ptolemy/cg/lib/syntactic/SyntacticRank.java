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

///////////////////////////////////////////////////////////////////
//// SyntacticRank

/**
 This class represents the rank of a SyntacticTerm. The rank is
 composed of four positive numbers referring respectively to the
 number of forward outputs, reverse outputs, forward inputs, and
 reverse inputs to the term. The reverse i/o, while supported
 in this type has not been completely assigned semantics.
 Currently, the forward elements are only used to characterize
 the compositionality of terms under combinational operations.

 At the least, the reverse versions follow an analogy from
 tensor ranks in linear algebra, where forward ranks refer
 to vector spaces and reverse ranks to co-vector spaces. I
 might use this concept to codify reverse propagation of
 feedback edges against the feed-forward edges that constitute
 the acyclic component of a term.

@author Chris Shaver
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (shaver)
@Pt.AcceptedRating Red
 */
public class SyntacticRank {

    /** Construct a new rank.
     *  See the class description for an explanation of
     *  these ranks.
     *
     * @param fo Forward output rank.
     * @param ro Reverse output rank.
     * @param fi Forward input rank.
     * @param ri Reverse input rank.
     */
    public SyntacticRank(int fo, int ro, int fi, int ri) {
        _rank = new int[_dimension];
        _rank[_forwardOut] = fo;
        _rank[_reverseOut] = ro;
        _rank[_forwardIn] = fi;
        _rank[_reverseIn] = ri;
    }

    /** Construct a new forward rank.
     *  Reverse ranks are set to 0.
     *  See the class description for an explanation of
     *  these ranks.
     *
     * @param fo Forward output rank.
     * @param fi Forward input rank.
     */
    public SyntacticRank(int fi, int fo) {
        _rank = new int[_dimension];
        _rank[_forwardOut] = fo;
        _rank[_reverseOut] = 0;
        _rank[_forwardIn] = fi;
        _rank[_reverseIn] = 0;
    }

    /** Copy a rank.
     *
     * @return new copied rank.
     */
    public SyntacticRank copy() {
        return new SyntacticRank(_rank[_forwardOut], _rank[_reverseOut],
                _rank[_forwardIn], _rank[_reverseIn]);
    }

    /** Get the forward output rank.
     *  See the class description for an explanation of
     *  these ranks.
     *
     *  @return forward output rank.
     */
    public int forwardOut() {
        return _rank[_forwardOut];
    }

    /** Get the reverse output rank.
     *  See the class description for an explanation of
     *  these ranks.
     *
     *  @return reverse output rank.
     */
    public int reverseOut() {
        return _rank[_reverseOut];
    }

    /** Get the forward input rank.
     *  See the class description for an explanation of
     *  these ranks.
     *
     *  @return forward input rank.
     */
    public int forwardIn() {
        return _rank[_forwardIn];
    }

    /** Get the reverse input rank.
     *  See the class description for an explanation of
     *  these ranks.
     *
     *  @return reverse input rank.
     */
    public int reverseIn() {
        return _rank[_reverseIn];
    }

    /** Generate code representation of rank.
     *  The form of this representation is as follows:
     *  <br> &lt;forward in (reverse out) -&gt; forward out (reverse in)&gt;
     *
     *  <br> or, if the reverse components are 0:
     *
     *  <br>   &lt;forward in -&gt; forward out&gt;
     *
     *  @return code representation of rank.
     */
    public String generateCode() {
        if (_rank[_reverseOut] == 0 && _rank[_reverseIn] == 0) {
            return "<" + _rank[_forwardIn] + " -> " + _rank[_forwardOut] + ">";
        } else {
            return "<" + +_rank[_forwardIn] + "(" + _rank[_reverseOut] + ")"
                    + " -> " + _rank[_forwardOut] + "(" + _rank[_reverseIn]
                            + ")" + ">";
        }
    }

    /** Generate code representation for no rank information.
     *
     *  @return code representation of no rank.
     */
    static public String noCode() {
        return "<>";
    }

    // TODO: Add reverse rank logic.
    /** Compose two ranks if possible, else return null.
     *  Ranks <i>A</i> and <i>B</i> can be composed if the number of forward
     *  outputs of B is equal to the number of forward inputs of A. If reverse
     *  connections exist, the number of reverse outputs of <i>B</i> must be
     *  equal to the number of reverse inputs of <i>A</i>. (Note that this
     *  order is in fact opposite of the forward case since reverse connections
     *  form in the opposite direction.)
     *
     *  Note that this composition operation should be interpreted as the first
     *  rank going into the second rank, rather than the first rank operating on
     *  the second as in function composition notation.
     *
     *  @param a First rank.
     *  @param b Second rank.
     *  @return composition of first and second rank.
     */
    static public SyntacticRank compose(SyntacticRank a, SyntacticRank b) {
        boolean canCompose = a.forwardOut() == b.forwardIn();
        if (!canCompose) {
            System.out.print("Composition problem");
        }
        return canCompose ? new SyntacticRank(a.forwardIn(), b.forwardOut())
        : null;
    }

    /** Compose the ranks of two terms.
     *
     * @param a First term.
     * @param b Second term.
     * @return composition of the ranks of the first and second term.
     * @see SyntacticRank#compose
     */
    static public SyntacticRank compose(SyntacticTerm a, SyntacticTerm b) {
        return compose(a.rank(), b.rank());
    }

    // TODO: Add reverse rank logic, and use copy function.
    /** Add the two ranks if possible, else return null.
     *  Ranks are only able to be added if they are the same, and the result
     *  is simply the same rank as the operands. A copy is returned. This is
     *  a operation rather than just a check for purposes of modularity. An
     *  operation adding terms can call this to be symmetric with other operations.
     *
     *  @param a First rank.
     *  @param b Second rank.
     *  @return sum of first and second ranks.
     */
    static public SyntacticRank add(SyntacticRank a, SyntacticRank b) {
        return a.forwardIn() == b.forwardIn()
                && a.forwardOut() == b.forwardOut() ? new SyntacticRank(
                        a.forwardIn(), a.forwardOut()) : null;
    }

    /** Add the ranks of two terms.
     *
     *  @param a First term.
     *  @param b Second term.
     *  @return sum of the ranks of the first and second terms.
     *  @see #add(SyntacticRank, SyntacticRank)
     */
    static public SyntacticRank add(SyntacticTerm a, SyntacticTerm b) {
        return add(a.rank(), b.rank());
    }

    // TODO: Add reverse rank logic.
    /** Multiply two ranks.
     *  This is always possible, following the analogy from tensor products.
     *  The product is simply the element-wise sum of the ranks.
     *
     *  @param a First rank.
     *  @param b Second rank.
     *  @return product of ranks.
     */
    static public SyntacticRank product(SyntacticRank a, SyntacticRank b) {
        return new SyntacticRank(a.forwardIn() + b.forwardIn(), a.forwardOut()
                + b.forwardOut());
    }

    /** Multiply the ranks of two terms.
     *
     * @param a First term.
     * @param b Second term.
     * @return product of the ranks of first and second term.
     */
    static public SyntacticRank product(SyntacticTerm a, SyntacticTerm b) {
        return product(a.rank(), b.rank());
    }

    // TODO: Add reverse rank logic. Is this possible with reverses?
    /** Contract rank about a given number of input/output pairs.
     *  This is possible when the number of contracted pairs are fewer
     *  than then smaller of the numbers of inputs and outputs.
     *
     *  @param a Rank to contract.
     *  @param n Number of contractions.
     *  @return Contracted rank or null if not possible.
     */
    static public SyntacticRank contract(SyntacticRank a, int n) {
        if (n < 0 || n > a.forwardIn() || n > a.forwardOut()) {
            return null;
        }
        return new SyntacticRank(a.forwardIn() - n, a.forwardOut() - n);
    }

    /** Multiply with a given rank.
     *
     *  @param a Rank to multiply by.
     *  @return reference to this.
     *  @see #product(SyntacticRank, SyntacticRank)
     */
    public SyntacticRank product(SyntacticRank a) {
        _rank[_forwardIn] += a.forwardIn();
        _rank[_forwardOut] += a.forwardOut();
        return this;
    }

    // TODO: Add reverse rank logic.
    /** Divide by given rank.
     *  Division is simply point-wise subtraction in this case.
     *
     *  @param a Divisor rank.
     *  @return reference to this.
     *  @see #product(SyntacticRank, SyntacticRank)
     */
    public SyntacticRank quotent(SyntacticRank a) {
        _rank[_forwardIn] = Math.max(_rank[_forwardIn] - a.forwardIn(), 0);
        _rank[_forwardOut] = Math.max(_rank[_forwardOut] - a.forwardOut(), 0);
        return this;
    }

    /** Forward output index. */
    private static final int _forwardOut = 0;

    /** Reverse output index. */
    private static final int _reverseOut = 1;

    /** Forward input index. */
    private static final int _forwardIn = 2;

    /** Reverse input index. */
    private static final int _reverseIn = 3;

    /** Dimension of rank. */
    private static final int _dimension = 4;

    /** Rank stored as an array. */
    private int _rank[];
}
