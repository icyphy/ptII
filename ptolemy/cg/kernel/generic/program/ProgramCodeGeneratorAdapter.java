/* Base class for program code generator adapter.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic.program;

///////////////////////////////////////////////////////////////////////
////ProgramCodeGeneratorAdapter

/*
* @author Bert Rodiers
* @version $Id$
* @since Ptolemy II 8.0
* @Pt.ProposedRating Red (rodiers)
* @Pt.AcceptedRating Red (rodiers)
*/

import ptolemy.actor.IOPort;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.util.StringUtilities;

public class ProgramCodeGeneratorAdapter extends CodeGeneratorAdapter {

    /** Construct the code generator adapter associated
     *  with the given component.
     *  @param component The associated component.
     */
    public ProgramCodeGeneratorAdapter(Object component) {
        _component = component;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                 ////

    /** Get the code generator adapter associated with the given component.
     *  @param component The given component.
     *  @return The code generator adapter.
     *  @exception IllegalActionException If the adapter class cannot be found.
     */
    public final ProgramCodeGeneratorAdapter getAdapter(Object component) throws IllegalActionException {
        return (ProgramCodeGeneratorAdapter) getCodeGenerator().getAdapter(
                component);
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    public ProgramCodeGenerator getCodeGenerator() {
        return _strategy.getCodeGenerator();
    }

    /** Get the component associated with this adapter.
     *  @return The associated component.
     */
    public Object getComponent() {
        return _strategy.getComponent();
    }

    /** Get the strategy associated with this adapter.
     *  @return The associated strategy.
     *   @see #setStrategy
     */
    public ProgramCodeGeneratorAdapterStrategy getStrategy() {
        return _strategy;
    }

    /** Process the specified code, replacing macros with their values.
     * @param code The code to process.
     * @return The processed code.
     * @exception IllegalActionException If illegal macro names are found.
     */
    public final String processCode(String code) throws IllegalActionException {
        return _strategy.getTemplateParser().processCode(code);
    }

    /** Set the strategy for generating code for this adapter.
     * @param strategy The strategy.
     * @see #getStrategy
     */
    public final void setStrategy(Object strategy) {
        _strategy = (ProgramCodeGeneratorAdapterStrategy) strategy;
        _strategy.setComponent(_component);
        _strategy.setAdapter(this);
    }

    /** Set the code generator associated with this adapter class.
     *  @param codeGenerator The code generator associated with this
     *   adapter class.
     *  @see #getCodeGenerator()
     */
    public void setCodeGenerator(GenericCodeGenerator codeGenerator) {
        _strategy.setCodeGenerator((ProgramCodeGenerator) codeGenerator);
    }
    
    public String toString() {
        return getComponent().toString() + "'s Adapter";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Generate code for a given block.  The comment includes
     *  the portion of the blockName parameter up until the string
     *  "Block".
     *  @param blockName The name of the block, which usually ends
     *  with the string "Block".
     *  @return The generated wrapup code.
     *  @exception IllegalActionException If thrown while appending to the
     *  the block or processing the macros.
     */
    protected String _generateBlockByName(String blockName)
            throws IllegalActionException {
        CodeStream codeStream = getStrategy().getTemplateParser().getCodeStream();
        codeStream.clear();
        codeStream.appendCodeBlock(blockName, true);
        // There is no need to generate comment for empty code block.
        if (!codeStream.isEmpty()) {
            // Don't die if the blockName ends not in "Block".
            String shortBlockName = null;
            int index = blockName.lastIndexOf("Block");
            if (index != -1) {
                shortBlockName = blockName.substring(0, index);
            } else {
                shortBlockName = blockName;
            }
            
            if (getComponent() instanceof Nameable) {

                codeStream.insert(0, _eol
                        + getCodeGenerator().comment(shortBlockName
                                + (((Nameable) getComponent()).getName())));
            }
        }
        return processCode(codeStream.toString());

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////
    
    /** The associated component. */
    protected Object _component;

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;
    // FIXME rodiers: This is defined multiple times, in different root classes
    //              Not really what we want.

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    /** The strategy for generating code for this adapter.*/
    protected ProgramCodeGeneratorAdapterStrategy _strategy;


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     * The code block table that stores the code block body (StringBuffer)
     * with the code block name (String) as key.
     */
    protected static final String[] _defaultBlocks = { "preinitBlock",
            "initBlock", "fireBlock", "postfireBlock", "wrapupBlock" };
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A class that defines a channel object. A channel object is
     *  specified by its port and its channel index in that port.
     */
    public static class Channel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
    
        /** Construct the channel with the given port and channel number.
         * @param portObject The given port.
         * @param channel The channel number of this object in the given port.
         */
        public Channel(IOPort portObject, int channel) {
            port = portObject;
            channelNumber = channel;
        }
    
        /**
         * Whether this channel is the same as the given object.
         * @param object The given object.
         * @return True if this channel is the same reference as the given
         *  object, otherwise false;
         */
        public boolean equals(Object object) {
            return object instanceof Channel
                    && port.equals(((Channel) object).port)
                    && channelNumber == ((Channel) object).channelNumber;
        }
    
        /**
         * Return the hash code for this channel. Implementing this method
         * is required for comparing the equality of channels.
         * @return Hash code for this channel.
         */
        public int hashCode() {
            return port.hashCode() + channelNumber;
        }
    
        /**
         * Return the string representation of this channel.
         * @return The string representation of this channel.
         */
        public String toString() {
            return port.getName() + "_" + channelNumber;
        }
    
        /** The port that contains this channel.
         */
        public IOPort port;
    
        /** The channel number of this channel.
         */
        public int channelNumber;
    }


}
