/* An actor that executes compiled embedded C code.

 Copyright (c) 2007-2010 The Regents of the University of California.
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
package ptolemy.actor.lib.jni;

import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
////EmbeddedCActor

/**
 An actor of this class executes compiled embedded C Code.

 <p>To use this actor within Vergil, double click on the actor and
 insert C code into the code templates, as indicated by the sample
 template.  Normally you will also need to add ports to the actor.
 You may need to set the types of these ports as well.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class EmbeddedCActor extends CompiledCompositeActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructor,
     *  create the <i>embeddedCCode</i> parameter, and initialize
     *  it to provide an empty template.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception NameDuplicationException If the container already
     *   has an actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public EmbeddedCActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        embeddedCCode = new StringAttribute(this, "embeddedCCode");

        // Set the visibility to expert, as casual users should
        // not see the C code.  This is particularly true if one
        // installs an actor that is an instance of this with
        // particular C code in the library.
        embeddedCCode.setVisibility(Settable.EXPERT);

        // initialize the code to provide a template for identity function:
        //
        // /***fileDependencies***/
        // /**/
        //
        // /***preinitBlock***/
        // /**/
        //
        // /***initBlock***/
        // /**/
        //
        // /***fireBlock***/
        // // Assuming you have added an input port named "input"
        // // and an output port named "output", then the following
        // // line results in the input being copied to the output.
        // $put(output, $get(input));
        // /**/
        //
        // /***wrapupBlock***/
        // /**/
        //
        String code = "";
        code = code + _getFileDependencies();
        code = code + _getPreinitBlock();
        code = code + _getInitBlock();
        code = code + _getFireBlock();
        code = code + _getWrapupBlock();
        embeddedCCode.setExpression(code);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"62\" height=\"30\" "
                + "style=\"fill:black\"/>\n" + "<text x=\"-29\" y=\"4\""
                + "style=\"font-size:10; fill:white; font-family:SansSerif\">"
                + "EmbeddedC</text>\n" + "</svg>\n");

        // For embeddedCActor, there is only C code specifying its
        // functionality.  Therefore JNI has to be invoked when it is
        // executed.
        invokeJNI.setExpression("true");

        new SDFDirector(this, "SDFDirector");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The C code that specifies the function of this actor.  The
     *  default value is the code necessary to implement a identity
     *  function.
     *  The contents of this parameter contains blocks of C code
     *  that is invoked during various phases of execution.
     * 
     *  The syntax for <code>$put()</code> and other methods is
     *  documented in $PTII/doc/codegen.htm.
     */
    public StringAttribute embeddedCCode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the embedded actor and add ports to it.
     */
    public void preinitialize() throws IllegalActionException {
        try {
            setEmbeddedActor();

            int i = 0;
            for (TypedIOPort port : (List<TypedIOPort>) portList()) {
                TypedIOPort newPort = (TypedIOPort) port.clone(workspace());
                newPort.setContainer(_embeddedActor);
                for (int channel = 0; channel < port.getWidth(); channel++) {
                    TypedIORelation relation = new TypedIORelation(this,
                            "relation" + i++);
                    relation.setPersistent(false);
                    port.link(relation);
                    newPort.link(relation);
                }
            }
        } catch (NameDuplicationException ex) {
            throw new IllegalActionException(this, ex, "Name duplication.");
        } catch (CloneNotSupportedException ex) {
            throw new IllegalActionException(this, ex, "Clone not supported.");
        }
        super.preinitialize();
    }

    /** Remove inside relations.
     */
    public void wrapup() throws IllegalActionException {
        try {
            for (TypedIOPort port : (List<TypedIOPort>) portList()) {
                for (TypedIORelation relation : (List<TypedIORelation>) port
                        .insideRelationList()) {
                    relation.setContainer(null);
                }
            }
            // If an exception occurred earlier, then
            // _embeddedActor may be null, and we don't want
            // the null pointer exception masking the real
            // one.
            if (_embeddedActor != null) {
                _embeddedActor.setContainer(null);
            }
        } catch (NameDuplicationException ex) {
            // should not happen.
            throw new IllegalActionException(this, "name duplication.");
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Create a new instance instance of EmbeddedActor.
     * Derived classes should override this method and create their
     * own instances as necessary.
     *  @exception NameDuplicationException If the container already
     *   has an actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    protected void setEmbeddedActor() throws IllegalActionException,
            NameDuplicationException {

        // This code was separated into its own function so that
        // EmbeddedCFileActor can extend this class without a large
        // amount of code duplication.  This method needs to be
        // overwritten in EmbeddedCFileActor to create a new instance
        // of EmbeddedFileActor rather than EmbeddedActor.

        _embeddedActor = new EmbeddedActor(this, "EmbeddedActor");
    }

    /** Get the fileDependencies part of the generated code.
     *  @return The string containing the codegen fileDependencies function.
     */
    protected String _getFileDependencies() {
        String code = "/***fileDependencies***/\n" + "/**/\n\n";
        return code;
    }

    /** Get the fireBlock part of the generated code.
     *  @return The string containing the codegen fireBlock function.
     */
    protected String _getFireBlock() {
        String code = "/***fireBlock***/\n"
                + "// Assuming you have added an input port named \"input\"\n"
                + "// and an output port named \"output\", "
                + "then the following\n"
                + "// line results in the input being copied to the output.\n"
                + "//$put(output, $get(input));\n" + "/**/\n\n";
        return code;
    }

    /** Get the initBlock part of the generated code.
     *  @return The string containing the codegen initBlock function.
     */
    protected String _getInitBlock() {
        String code = "/***initBlock***/\n" + "/**/\n\n";
        return code;
    }

    /** Get the preinitBlock part of the generated code.
     *  @return The string containing the codegen preinitBlock function.
     */
    protected String _getPreinitBlock() {
        String code = "/***preinitBlock***/\n" + "/**/\n\n";
        return code;
    }

    /** Get the wrapupBlock part of the generated code.
     *  @return The string containing the codegen wrapupBlock function.
     */
    protected String _getWrapupBlock() {
        String code = "/***wrapupBlock***/\n" + "/**/\n\n";
        return code;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The embedded actor used to contain the ports to the C
     * implementation.
     */
    protected EmbeddedActor _embeddedActor = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** An actor inside the EmbeddedCActor that is used as a dummy
     *  placeholder.  The EmbeddedActor is created in preinitialize() where
     *  ports from the outer EmbeddedCActor are connected to the EmbeddedActor.
     *  The EmbeddedActor is destroyed in wrapup().
     */
    public static class EmbeddedActor extends TypedAtomicActor {
        /** Create a new instance of EmbeddedActor.
         *  @param container The container.
         *  @param name The name of this actor within the container.
         *  @exception IllegalActionException If this actor cannot be contained
         *   by the proposed container.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public EmbeddedActor(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            // In case an exception occurs and wrapup can't destroy
            // it, at least make sure it isn't saved.
            setPersistent(false);
        }
    }
}
