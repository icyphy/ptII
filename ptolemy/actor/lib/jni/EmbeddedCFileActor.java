/* An actor that executes compiled embedded C code.  It takes in a file
 parameter that specifies the file of C code. 

 Copyright (c) 2008 The Regents of the University of California.
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


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;



////EmbeddedCFileActor

/**
 An actor of this class executes compiled embedded C Code.
 This actor extends the EmbeddedCActor and has most of the same functionality.
 The only difference is that a file of C code can be passed in as a parameter
 to the EmbeddedCFileActor.  This avoids multiple instances of the same code
 in copies of the EmbeddedCActor.

 @author Christine Avanessians, Edward Lee, and Man-Kit Leung 
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (cavaness)
 @Pt.AcceptedRating Red (cavaness)
 */

public class EmbeddedCFileActor extends EmbeddedCActor {

    public EmbeddedCFileActor(CompositeEntity container, String name)
    throws NameDuplicationException, IllegalActionException {
        super(container, name);

        codeBlockFile = new FileParameter(this, "codeBlockFile");
        codeBlockFile.setExpression("");
        
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
    
    protected void setEmbeddedActor() throws IllegalActionException, NameDuplicationException{
        //_embeddedActor = new EmbeddedFileActor(this, "EmbeddedFileActor");
    }


    public FileParameter codeBlockFile;
    public File _codeFile;
    public long _timeOfModification=-1;

    /** An actor inside the EmbeddedCActor that is used as a dummy
     *  placeholder.  The EmbeddedActor is created in preinitialize() where
     *  ports from the outer EmbeddedCActor are connected to the EmbeddedActor.
     *  The EmbeddedActor is destroyed in wrapup().
     */
    public static class EmbeddedFileActor extends ptolemy.actor.lib.jni.EmbeddedCActor.EmbeddedActor {
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
