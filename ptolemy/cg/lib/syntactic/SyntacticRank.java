/* Interface for terms in Syntactic expressions.

Copyright (c) 2010 The Regents of the University of California.
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

public class SyntacticRank {
    public SyntacticRank(int fo, int ro, int fi, int ri) {
        _rank = new int[_dimension];
        _rank[_forwardOut] = fo;
        _rank[_reverseOut] = ro;
        _rank[_forwardIn]  = fi;
        _rank[_reverseIn]  = ri;
    }
    
    public SyntacticRank(int fi, int fo) {
        _rank = new int[_dimension];
        _rank[_forwardOut] = fo;
        _rank[_reverseOut] = 0;
        _rank[_forwardIn]  = fi;
        _rank[_reverseIn]  = 0;
    }
    
    public SyntacticRank copy() {
        return new SyntacticRank(_rank[_forwardOut], _rank[_reverseOut], 
                        _rank[_forwardIn],  _rank[_reverseIn]);
    }
    
    public int forwardOut() {
        return _rank[_forwardOut];
    }
    
    public int reverseOut() {
        return _rank[_reverseOut];
    }
    
    public int forwardIn() {
        return _rank[_forwardIn];
    }
    
    public int reverseIn() {
        return _rank[_reverseIn];
    }
    
    public String generateCode() {
        if (_rank[_reverseOut] == 0 && _rank[_reverseIn] == 0)
            return "<" + _rank[_forwardIn] + " -> " + _rank[_forwardOut] + ">";
        else return "<" + 
            + _rank[_forwardIn]  + "(" + _rank[_reverseOut] + ")" + " -> "
            + _rank[_forwardOut] + "(" + _rank[_reverseIn]  + ")" + ">";
    }
    
    static public String noCode() {
        return "<>";
    }
    
    static public SyntacticRank compose(SyntacticRank a, SyntacticRank b) {
        boolean canCompose = a.forwardOut() == b.forwardIn();
        if (!canCompose) System.out.print("Composition problem");
        return canCompose 
            ? new SyntacticRank(a.forwardIn(), b.forwardOut()) : null;
    }
    
    static public SyntacticRank compose(SyntacticTerm a, SyntacticTerm b) {
        return compose(a.rank(), b.rank());
    }
    
    static public SyntacticRank add(SyntacticRank a, SyntacticRank b) {
        return a.forwardIn() == b.forwardIn() && a.forwardOut() == b.forwardOut()
            ? new SyntacticRank(a.forwardIn(), a.forwardOut()) : null;
    }
    
    static public SyntacticRank add(SyntacticTerm a, SyntacticTerm b) {
        return add(a.rank(), b.rank());
    }
    
    static public SyntacticRank product(SyntacticRank a, SyntacticRank b) {
        return new SyntacticRank(a.forwardIn() + b.forwardIn(), a.forwardOut() + b.forwardOut());
    }
    
    static public SyntacticRank contract(SyntacticRank a, int n) {
        if (n < 0 || n > a.forwardIn() || n > a.forwardOut()) return null;
        return new SyntacticRank(a.forwardIn() - n, a.forwardOut() - n);
    }
    
    public SyntacticRank product(SyntacticRank a) {
        _rank[_forwardIn]   += a.forwardIn();
        _rank[_forwardOut]  += a.forwardOut();
        return this;
    }
    
    public SyntacticRank quotent(SyntacticRank a) {
        _rank[_forwardIn]   = Math.max(_rank[_forwardIn]  - a.forwardIn(), 0);
        _rank[_forwardOut]  = Math.max(_rank[_forwardOut] - a.forwardOut(), 0);
        return this;
    }
    
    static public SyntacticRank product(SyntacticTerm a, SyntacticTerm b) {
        return product(a.rank(), b.rank());
    }
    
    
    private static final int _forwardOut = 0;
    private static final int _reverseOut = 1;
    private static final int _forwardIn  = 2;
    private static final int _reverseIn  = 3;
    private static final int _dimension  = 4;
    
    private int _rank[];
}
