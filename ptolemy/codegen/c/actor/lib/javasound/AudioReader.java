/*
@Copyright (c) 2005 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.codegen.c.actor.lib.javasound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;

/**
* A helper class for ptolemy.actor.lib.javasound.AudioReader.
* 
* @author Man-Kit Leung
* @version $Id$
* @since Ptolemy II 5.1
* @Pt.ProposedRating Yellow (mankit)
* @Pt.AcceptedRating Yellow (mankit)
*/
public class AudioReader extends CCodeGeneratorHelper {

   /**
    * Constructor method for the AudioReader helper.
    * @param actor the associated actor.
    */
   public AudioReader(ptolemy.actor.lib.javasound.AudioReader actor) {
       super(actor);
   }
   

   /**
    * Generate fire code.
    * Read the <code>fireBlock</code> from AudioReader.c and append
    * into the given code buffer.
    * @param code the given buffer to append the code to.
    * @exception IllegalActionException If the code stream encounters an
    *  error in processing the specified code block(s).
    */
   public void generateFireCode(StringBuffer code)
           throws IllegalActionException {
       code.append(_generateBlockCode("fireBlock"));
   }
   
   /** 
    * Generate initialization code.
    * Get the file path from the actor's fileOrURL parameter. Read the
    * <code>initBlock</code> from AudioReader.c and pass the file path
    * string as an argument to code block. Replace macros with their values
    * and return the processed code string.
    * @return The processed code string.
    * @exception IllegalActionException If the file path parameter is invalid
    *  or the code stream encounters an error in processing the specified code
    *  block(s).
    */
   public String generateInitializeCode() throws IllegalActionException {
       super.generateInitializeCode();
       _codeStream.clear();
       ptolemy.actor.lib.javasound.AudioReader actor = 
           (ptolemy.actor.lib.javasound.AudioReader) getComponent();
       String fileNameString;

       try {
           fileNameString = FileUtilities.nameToFile(
                   actor.fileOrURL.getExpression(), null).getCanonicalPath();
           fileNameString = fileNameString.replace('\\', '/');
       } catch (IOException e) {
           throw new IllegalActionException("Cannot find file: "
                   + actor.fileOrURL.getExpression());
       }
       ArrayList args = new ArrayList();
       args.add(fileNameString);
       _codeStream.appendCodeBlock("initBlock", args);
       return processCode(_codeStream.toString());
   }

   /**
    * Generate preinitialization code.
    * Read the <code>preinitBlock</code> from AudioReader.c, replace 
    * macros with their values and return the processed code string.
    * @return The processed code string.
    * @exception IllegalActionException If the code stream encounters an
    *  error in processing the specified code block(s).
    */
   public String generatePreinitializeCode() throws IllegalActionException {
       super.generatePreinitializeCode();
       return _generateBlockCode("preinitBlock");
   }

   /**
    * Generate shared code.
    * Read the <code>sharedBlock</code> from AudioReader.c,
    * replace macros with their values and append the processed code
    * block to the given code buffer.
    * @param code the given buffer to append the code to.
    * @return The processed code string.
    * @exception IllegalActionException If the code stream encounters an
    *  error in processing the specified code block(s).
    */
   public String generateSharedCode() throws IllegalActionException {
       // We don't need to process the code block here because the
       // sharedCode do not contain any macros.
       return _generateBlockCode("sharedBlock", false);
   }

   /** 
    * Generate wrap up code.
    * Read the <code>wrapupBlock</code> from AudioReader.c, replace
    * macros with their values and append the processed code block
    * to the given code buffer.
    * @param code the given buffer to append the code to.
    * @exception IllegalActionException If the code stream encounters an
    *  error in processing the specified code block(s).
    */
   public void generateWrapupCode(StringBuffer code)
           throws IllegalActionException {
       code.append(_generateBlockCode("wrapupBlock"));
   }
   /** 
    * Get the files needed by the code generated for the
    * AudioReader actor.
    * @return A set of Strings that are names of the files
    *  needed by the code generated for the AudioReader actor.
    */
   public Set getIncludingFiles() {
       Set files = new HashSet();
       files.add("<math.h>");
       files.add("<stdio.h>");
       files.add("\"SDL.h\"");
       files.add("\"SDL_audio.h\"");
       return files;
   }
}
