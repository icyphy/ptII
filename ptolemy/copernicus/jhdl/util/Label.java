/* Control flow label

 Copyright (c) 2001-2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.util;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;

/**
 * Control flow label
 */
class Label {
    /**
     * The head of a new tree
     */
    public Label(){
	this(null, false, null);
    }

    public Label(Label parent, boolean branch, SuperBlock block){
	_parent = parent;
	_branch = branch;
	_block = block;

	if (_parent == null){
	    _level = 0;
	} else {
	    _level = _parent._level + 1;
	    _parent.addChild(this, branch);
	}

    }

    public void addChild(Label child, boolean branch){
	if (branch){
	    _trueChild=child;
	} else {
	    _falseChild=child;
	}
    }

    /**
     * Return whether this Label is the true or false case of the beginCondition
     */
    public boolean branch(){
	return _branch;
    }

    public int level(){
	return _level;
    }

    public Label getParent(){
	return _parent;
    }

    public boolean canCombine(Label l){
	return (_parent == l._parent);
    }

    public SuperBlock getSuperBlock(){
	return _block;
    }

//      public ConditionExpr beginCondition(){
//  	return _beginCondition;
//      }

    public ConditionExpr endCondition(){
	//return _endCondition;
	return null;
    }

    /**
     * This Label's parent
     */
    protected Label _parent;

    /**
     * Which branch of the parent is this label?
     */
    protected boolean _branch;

    protected Label _trueChild;
    protected Label _falseChild;

    /**
     * How deep in the tree is this Label
     */
    protected int _level = 0;

//      /**
//       * Each Label has a beginning condition, which is the condition that caused
//       * this label to be created
//       */
//      protected ConditionExpr _beginCondition = null;

    /**
     * A Label can have an end condition, where this label is "split" into
     * two new, longer labels
     */
    //protected ConditionExpr _beginCondition = null;

    protected SuperBlock _block;
}
