/* An actor that executes compiled embedded code.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.cg.lib;

import java.lang.reflect.Constructor;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// EmbeddedCodeActor

/**
 An actor of this class executes compiled embedded code.
 FIXME: Currently, it seems this only works with embedded Java code, but
 in theory it should work with any embedded code that can be executed
 within Java.
 <p>
 To use this actor within Vergil, double click on the actor and
 insert Java code into the code templates, as indicated by the sample
 template.  Normally you will also need to add ports to the actor.
 You may need to set the types of these ports as well.
 <p>
 This actor is actually a composite actor that contains a single
 embedded actor that actually executes the generated code. The
 reason for the extra level of hierarchy is so that the director
 adapter code that handles conversion and transport across the boundary
 can be consolidated in one place. In its preinitialize() method,
 this actor will create an instance of whatever director is
 in charge of executing it. The presumption is that that director
 has a code generation adapter that knows how to transport data
 from the simulation world in Java to the generated code world
 within.


 @author Bert Rodiers, Gang Zhou, Edward A. Lee, Jia Zou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class EmbeddedCodeActor extends CompiledCompositeActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructor,
     *  create the <i>embeddedCode</i> parameter, and initialize
     *  it to provide an empty template.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception NameDuplicationException If the container already
     *   has an actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public EmbeddedCodeActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        embeddedCode = new StringAttribute(this, "embeddedCode");

        // Set the visibility to expert, as casual users should
        // not see the Java code.  This is particularly true if one
        // installs an actor that is an instance of this with
        // particular Java code in the library.
        embeddedCode.setVisibility(Settable.EXPERT);

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
        embeddedCode.setExpression(code);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"75\" height=\"30\" "
                + "style=\"fill:black\"/>\n" + "<text x=\"-29\" y=\"4\""
                + "style=\"font-size:10; fill:white; font-family:SansSerif\">"
                + "embeddedCode</text>\n" + "</svg>\n");

        // For embeddedJavaActor, there is only embedded Java code specifying its
        // functionality.
        executeEmbeddedCode.setExpression("true");

        // In preinitialize(), this actor will create a director that matches
        // the class of its enclosing director. However, we need it to have
        // a director at all times, so we set a generic director here.
        new Director(this, "Director");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The Java code that specifies the function of this actor.  The
     *  default value is the code necessary to implement a identity
     *  function.
     */
    public StringAttribute embeddedCode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the embedded actor and add ports to it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {

        Director executiveDirector = getExecutiveDirector();
        Director localDirector = getDirector();
        if (localDirector == null
                || !localDirector.getClass().equals(
                        executiveDirector.getClass())) {
            if (localDirector != null) {
                try {
                    localDirector.setContainer(null);
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }
            }
            // The inside director should match the outside director.
            // Use reflection to create a matching instance.
            Class directorClass = getExecutiveDirector().getClass();
            Constructor<?> constructor = null;
            try {
                constructor = directorClass.getConstructor(new Class[] {
                        CompositeEntity.class, String.class });
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Cannot create an instance of the enclosing director class: "
                                + directorClass);
            }
            try {
                constructor.newInstance(new Object[] { this,
                        getExecutiveDirector().getName() });
                localDirector = getDirector();
                if (localDirector instanceof SDFDirector) {
                    // Needed for $PTII/bin/vergil ptolemy/cg/lib/test/auto/Scale_c.xml
                    // See "SDF director iterations parameter default of 0 is unfriendly"
                    // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5546
                    ((SDFDirector) localDirector).iterations.setExpression("0");

                }
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to instantiate the enclosing director class: "
                                + directorClass);
            }
        }

        try {
            setEmbeddedActor();

            int i = 0;
            for (TypedIOPort port : (List<TypedIOPort>) portList()) {
                TypedIOPort newPort = (TypedIOPort) port.clone(workspace());
                newPort.setContainer(_embeddedActor);
                int width = port.getWidth();
                for (int channel = 0; channel < width; channel++) {
                    TypedIORelation relation = new TypedIORelation(this,
                            "relation" + i++);
                    relation.setPersistent(false);
                    relation.setWidth(1);
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
    @Override
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
        // embeddedJavaFileActor can extend this class without a large
        // amount of code duplication.  This method needs to be
        // overwritten in embeddedJavaFileActor to create a new instance
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
                + "//DOLLARput(output, DOLLARget(input));\n"
                + "//(replace DOLLAR with the dollar sign.  The problem "
                + "//is that the code generator does substitution on the dollar sign\n"
                + "/**/\n\n";
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

    /** An actor inside the embeddedJavaActor that is used as a dummy
     *  placeholder.  The EmbeddedActor is created in preinitialize() where
     *  ports from the outer embeddedJavaActor are connected to the EmbeddedActor.
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
