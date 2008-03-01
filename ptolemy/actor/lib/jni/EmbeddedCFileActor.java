/* An actor that executes compiled embedded C code.  It takes in a file
 * parameter that specifies the file of C code. 

 Copyright (c) 2007 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

import java.io.*;
import java.util.Iterator;
import java.util.List;


////EmbeddedCFileActor

/**
 An actor of this class executes compiled embedded C Code.
 This actor extends the EmbeddedCActor and has most of the same functionality.
 The only difference is that a file of C code can be passed in as a parameter
 to the EmbeddedCFileActor.  This avoids multiple instances of the same code
 in copies of the EmbeddedCActor.

 @author Christine Avanessians, Edward Lee, and Man-Kit Leung 
 @version $Id
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cavaness)
 @Pt.AcceptedRating Red (cavaness)
 */

public class EmbeddedCFileActor extends CompiledCompositeActor {

    public EmbeddedCFileActor(CompositeEntity container, String name)
    throws NameDuplicationException, IllegalActionException {
        super(container, name);

        embeddedCCode = new StringAttribute(this, "embeddedCCode");

//      Set the visibility to expert, as casual users should
//      not see the C code.  This is particularly true if one
//      installs an actor that is an instance of this with
//      particular C code in the library.
        embeddedCCode.setVisibility(Settable.EXPERT);

        codeBlockFile = new FileParameter(this, "codeBlockFile");
        codeBlockFile.setExpression("");
        
//      initialize the code to provide a template for identity function:

//      /***fileDependencies***/
//      /**/

//      /***preinitBlock***/
//      /**/

//      /***initBlock***/
//      /**/

//      /***fireBlock***/
//      // Assuming you have added an input port named "input"
//      // and an output port named "output", then the following
//      // line results in the input being copied to the output.
//      $ref(output) = $ref(input);
//      /**/

//      /***wrapupBlock***/
//      /**/

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

//      For embeddedCActor, there is only C code specifying its
//      functionality.  Therefore JNI has to be invoked when it is
//      executed.
        invokeJNI.setExpression("true");

        new SDFDirector(this, "SDFDirector");
    }
    
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == codeBlockFile) {
            if (!(codeBlockFile.getExpression()).equals("")){
                File tempFile = codeBlockFile.asFile();
                if (tempFile.exists()){
                    //System.out.println("changing attribute");
                    _timeOfModification = tempFile.lastModified();
                    _codeFile=tempFile;
                }
                else {
                    /* Do nothing if the file does not exist.  Text editor will create a file with
                     * the saved file name.
                     */
                }
            }
            else {
                /* Do nothing if the file name is not specified.  Text editor will create a file with
                 * the saved file name.
                 */
            }
        }
        else{
            super.attributeChanged(attribute);
        }
    }

    protected void _generateAndCompileCCode() throws IllegalActionException {
        if (_codeFile.lastModified() > _timeOfModification){
            attributeChanged(codeBlockFile);
        }

        changeEmbeddedCCode();
        //System.out.println(embeddedCCode.getExpression());

        //System.out.println("generating code");

        super._generateAndCompileCCode();
    }

    public void changeEmbeddedCCode() throws IllegalActionException{
        BufferedReader reader = codeBlockFile.openForReading();

        if (reader == null){
            System.out.println("reader is null");
        }
        String code = new String();
        // Is there a better way of reading in file into a string???
        try{
            String str;
            while ((str = reader.readLine()) !=null){
                code = code.concat(str + "\n");
            }
        }catch (IOException e){
            // Does codeBlockFile.getExpression() actually get the name of the name of the file???
            throw new IllegalActionException ("Could not read file" + codeBlockFile.getExpression());
        }
        embeddedCCode.setExpression(code);
    }

    public FileParameter codeBlockFile;
    public File _codeFile;
    public long _timeOfModification=-1;

    

///////////////////////////////////////////////////////////////////
////ports and parameters                  ////

    /** The C code that specifies the function of this actor.
     *  The default value provides a template for an identity function.
     */
    public StringAttribute embeddedCCode;

///////////////////////////////////////////////////////////////////
////public methods                        ////

    public void preinitialize() throws IllegalActionException {
        try {
            _embeddedFileActor = new EmbeddedFileActor(this, "EmbeddedFileActor");

            int i = 0;
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                TypedIOPort port = (TypedIOPort) ports.next();
                TypedIOPort newPort = (TypedIOPort) port.clone(workspace());
                newPort.setContainer(_embeddedFileActor);
                for (int channel = 0; channel < port.getWidth(); channel++) {
                    TypedIORelation relation = new TypedIORelation(this,
                            "relation" + i++);
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

    public void wrapup() throws IllegalActionException {
        try {
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                TypedIOPort port = (TypedIOPort) ports.next();
                List insideRelationList = port.insideRelationList();
                Iterator insideRelationIterator = insideRelationList.iterator();
                while (insideRelationIterator.hasNext()) {
                    TypedIORelation relation = (TypedIORelation) insideRelationIterator
                    .next();
                    relation.setContainer(null);
                }
            }
            // If an exception occurred earlier, then
            // _embeddedActor may be null, and we don't want
            // the null pointer exception masking the real
            // one.
            if (_embeddedFileActor != null) {
                _embeddedFileActor.setContainer(null);
            }
        } catch (NameDuplicationException ex) {
            // should not happen.
            throw new IllegalActionException(this, "name duplication.");
        }
        super.wrapup();
    }

///////////////////////////////////////////////////////////////////
////protected methods                 ////

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
            + "// and an output port named \"output\", then the following\n"
            + "// line results in the input being copied to the output.\n"
            + "//$ref(output) = $ref(input);\n" + "/**/\n\n";
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
////private methods                   ////

    private EmbeddedFileActor _embeddedFileActor = null;

    /** An actor inside the EmbeddedCActor that is used as a dummy
     *  placeholder.  The EmbeddedActor is created in preinitialize() where
     *  ports from the outer EmbeddedCActor are connected to the EmbeddedActor.
     *  The EmbeddedActor is destroyed in wrapup().
     */
    public static class EmbeddedFileActor extends TypedAtomicActor {
        /** Create a new instance of EmbeddedActor.
         *  @param container The container.
         *  @param name The name of this actor within the container.
         *  @exception IllegalActionException If this actor cannot be contained
         *   by the proposed container.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public EmbeddedFileActor(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
            super(container, name);
            // In case an exception occurs and wrapup can't destroy
            // it, at least make sure it isn't saved.
            setPersistent(false);
        }
    }
}
