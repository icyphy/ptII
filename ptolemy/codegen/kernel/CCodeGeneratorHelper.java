/* Base class for C code generator helper.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.codegen.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// CCodeGeneratorHelper

/**
 Base class for C code generator helper. It overrides the
 generateFireCode(), generateInitializeCode(), generatePreinitializeCode(),
 and generateWrapupCode() methods by appending a corresponding code block.
 Subclasses may override these methods if they have to do fancier things.

 @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Ye Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (eal)
 */
public class CCodeGeneratorHelper extends CodeGeneratorHelper {
    /**
     * Create a new instance of the C code generator helper.
     * @param component The actor object for this helper.
     */
    public CCodeGeneratorHelper(NamedObj component) {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate the fire code. In this base class, do nothing. Subclasses
     * may extend this method to generate the fire code of the associated
     * component and append the code to the given string buffer.
     * @param code The given string buffer.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        super.generateFireCode(code);
    }

    /**
     * Generate the initialize code. In this base class, return an empty
     * string. Subclasses may extend this method to generate the initialize
     * code of the associated component and append the code to the given
     * string buffer.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();
        return "";
    }

    /**
     * Generate the preinitialize code. In this base class, return an empty
     * string. This method generally does not generate any execution code
     * and returns an empty string. Subclasses may generate code for variable
     * declaration, defining constants, etc.
     * @return A string of the preinitialize code for the helper.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        return "";
    }

    /**
     * Generate the shared code. This is the FIRST generate method invoked out
     * of all, so any initializations of variables of this helper should be
     * done in this method. In this base class, return an empty set. Subclasses
     * may generate code for variable declaration, defining constants, etc.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set generateSharedCode() throws IllegalActionException {
        super.generateSharedCode();
        return new HashSet();
    }

    /**
     * Generate the wrapup code. This is the LAST generate method invoked out
     * of all, so any resets of variables of this helper should be done
     * in this method. In this base class, do nothing. Subclasses may extend
     * this method to generate the wrapup code of the associated component
     * and append the code to the given string buffer.
     * @param code The given string buffer.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupCode() throws IllegalActionException {
        super.generateWrapupCode();
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Given a block name, generate code for that block.
     *  This method is called by actors helpers that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the helper .c file.
     */
    protected String _generateBlockCode(String blockName)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        _codeStream.clear();
        _codeStream.appendCodeBlock(blockName);
        return processCode(_codeStream.toString());
    }

    /** Given a block name, generate code for that block.
     *  This method is called by actors helpers that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @param args The arguments for the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the helper .c file.
     */
    protected String _generateBlockCode(String blockName, ArrayList args)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        _codeStream.clear();
        _codeStream.appendCodeBlock(blockName, args);
        return processCode(_codeStream.toString());
    }

    /** Given a block name, generate code for that block.
     *  This method is called by actors helpers that have simple blocks
     *  that do not take parameters or have widths. This method gives user
     *  the freedom to indicate if the code block needs marco-processing.
     *  @param blockName The name of the block.
     *  @param process Flag to indicate if this block need marco processing.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the helper .c file.
     */
    protected String _generateBlockCode(String blockName, boolean process)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        _codeStream.clear();
        _codeStream.appendCodeBlock(blockName);

        if (process) {
            return processCode(_codeStream.toString());
        } else {
            return _codeStream.toString();
        }
    }

    //protected void 
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The code stream associated with this helper.
     */
    protected CodeStream _codeStream = new CodeStream(this);
}
