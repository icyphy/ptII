/* A Java visitor that computes indentation information
   for use when formatting source code derived from abstract syntax trees.

Copyright (c) 2001 The University of Maryland.  All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/


/*
    FIXME: most or all of this functionality should probably
    be migrated into ptolemy.lang.java. The question is
    whether or not any of this indentation computation functionality 
    is target-specific.

    FIXME: Use of the _transitionVisit() method is overdone.
    Most of the required transition setting is achieved through
    Block nodes. Use of _transitionVisit() elsewhere should
    be carefully re-evaluated.
*/

package ptolemy.lang.c;

import java.util.LinkedList;
import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;
import ptolemy.lang.java.JavaVisitor;

/** A JavaVisitor that sets indentation levels associated with 
 *  relevant nodes in the abstract syntax tree (AST). These
 *  indentation levels can be used during code generation to
 *  format the output source.
 *
 *  Indentation levels are stored as AST node properties through
 *  the INDENTATION_KEY index.
 *
 *  The indentation property of an AST node indicates roughly to what degree
 *  the associated code is to be indented in the output source, although
 *  the actual indentation must generally be determined in conjunction 
 *  with the semantics of the target language. 
 *
 *  Each unit of indentation represents a number of characters 
 *  that is determined by a code generation constant.
 *
 *  The Boolean-valued indentation transition property of a node indicates
 *  that code that "branches off" from the node will generally be indented
 *  one position to the right relative to the node.
 *
 *  @author Shuvra S. Bhattacharyya 
 *  @version $Id$
 */
public class IndentationVisitor extends JavaVisitor implements CCodeGeneratorConstants {

    /**
     * This constructor simply sets the visitor traversal method.
     * This visitor visits parents before visiting
     * their respective children.
     */
    public IndentationVisitor() {
        super(TM_SELF_FIRST);
    }


    /**
     *  Set the indentation level to zero, and the transition indicator
     *  to false.
     *  @param same as method _defaultVisit
     *  @return same as method _defaultVisit
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        Integer levelSetting = new Integer(0);
        node.setProperty(INDENTATION_KEY, levelSetting);
        node.setProperty(INDENTATION_TRANSITION_KEY, new Boolean(false));
        return levelSetting;
    }


    /*
     *  Invoke a transition visit for a constructor declaration node.
     *  @param same as method _defaultVisit
     *  @return same as method _defaultVisit
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
          return _transitionVisit(node, args);
      }



    /*
     *  Invoke a transition visit for an interface declaration node.
     *  @param same as method _defaultVisit
     *  @return same as method _defaultVisit
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
          return _transitionVisit(node, args);
    }



    /*
     *  Invoke a transition visit for a block node.
     *  @param same as method _defaultVisit
     *  @return same as method _defaultVisit
     */
    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return _transitionVisit(node, args);
    }



    /*
     *  Invoke a transition visit for a switch statement node.
     *  @param same as method _defaultVisit
     *  @return same as method _defaultVisit
     */
    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        return _transitionVisit(node, args);
    }

    /*
     *  Invoke a transition visit for a switch statement case node.
     *  @param same as method _defaultVisit
     *  @return same as method _defaultVisit
     */
    public Object visitCaseNode(CaseNode node, LinkedList args) {
        return _transitionVisit(node, args);
    }


    /*
     *  Invoke a transition visit for a while- or do-loop node.
     *  @param same as method _defaultVisit
     *  @return same as method _defaultVisit
     */
    public Object visitLoopNode(LoopNode node, LinkedList args) {
        return _transitionVisit(node, args);
    }


    /*
     *  Invoke a transition visit for a for-loop node.
     *  @param same as method _defaultVisit
     *  @return same as method _defaultVisit
     */
    public Object visitForNode(ForNode node, LinkedList args) {
        return _transitionVisit(node, args);
    }


    /*
     *  Invoke a transition visit for a try node.
     *  @param same as method _defaultVisit
     *  @return same as method _defaultVisit
     */
    public Object visitTryNode(TryNode node, LinkedList args) {
        return _transitionVisit(node, args);
    }


    /**
     * This default visitation method is used for most AST nodes
     * during indentation computation.
     * The default visit simply sets the indentation level of an AST
     * node to be the same as that of its parent (if the parent
     * is not associated with an indentation transition), or
     * one more than that of its parent (if the parent is associated
     * with an indentation transition). The indentation transistion
     * property of the visited node is set to false.
     * @param node The AST node whose indentation level and transition
     * properties are to be set.
     * @param args Visitor arguments.
     * @return The indentation level that was assigned to the given
     * AST node during this visit.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        TreeNode parent = node.getParent();
        int level = 0;
        boolean parentTransition = false;
        if (parent!=null) {
            if (parent.hasProperty(INDENTATION_KEY)) {
                level = ((Integer)(parent.getDefinedProperty(INDENTATION_KEY)))
                        .intValue();               
                parentTransition = ((Boolean)(parent.getDefinedProperty(
                        INDENTATION_TRANSITION_KEY))).booleanValue();               
            }
        }
        if (parentTransition) level++;
        Integer levelSetting = new Integer(level);
        Boolean transitionSetting = new Boolean(false);
        node.setProperty(INDENTATION_KEY, levelSetting);
        node.setProperty(INDENTATION_TRANSITION_KEY, transitionSetting);

        return levelSetting;
    }

    /**
     *  This visitation method is used for AST nodes at which indentation
     *  transitions occur (e.g., Block nodes).
     *  @param node The AST node whose indentation level and transition
     *  properties are to be set.
     *  @param args Visitor arguments.
     *  @return The indentation level that was assigned to the given
     *  AST node during this visit.
     */
    protected Object _transitionVisit(TreeNode node, LinkedList args) {
        Object indentation = _defaultVisit(node, args);

        // Override the false transition setting from the default visit.
        Boolean transitionSetting = new Boolean(true);
        node.setProperty(INDENTATION_TRANSITION_KEY, new Boolean(true)); 

        return indentation;
    }

}
