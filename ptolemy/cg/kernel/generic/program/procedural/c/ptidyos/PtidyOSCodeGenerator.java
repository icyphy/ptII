/* Code generator for PtidyOS, which is implemented on C.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic.program.procedural.c.ptidyos;

import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel.PtidesPreemptiveEDFDirector;
import ptolemy.cg.kernel.generic.program.procedural.c.CCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////PtidyOSCodeGenerator

/** Base class for PtidyOS code generator.
 *
 *  @author Jia Zou, Jeff C. Jensen
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (jiazou)
 *  @Pt.AcceptedRating red (jiazou)
 */
public class PtidyOSCodeGenerator extends CCodeGenerator {
    /** Create a new instance of the PtidyOS code generator.
     *  @param container The container.
     *  @param name The name of the PtidyOS code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public PtidyOSCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code. This is the main entry point. The generateCode()
     *  of the super method is called. After which code is generated
     *  for the assembly file.
     *  @param code The code buffer into which to generate the code.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If a type conflict occurs or the model
     *  is running.
     */
    @Override
    public int generateCode(StringBuffer code) throws KernelException {
        fileExtension = ".c";
        int result = super.generateCode(code);
        _generateAdditionalCodeFiles();
        return result;
    }

    @Override
    public String generateTypeConvertCode() throws IllegalActionException {
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for the assembly file.
     *  @exception IllegalActionException If getAdapter() throws it, or if the
     *  PtidyOSCodeGenerator is not used in a Ptides environment.
     *
     */
    protected void _generateAdditionalCodeFiles() throws IllegalActionException {
        PtidesPreemptiveEDFDirector directorAdapter = null;
        Director director = ((TypedCompositeActor) getContainer())
                .getDirector();
        if (director instanceof ptolemy.domains.ptides.kernel.PtidesDirector) {
            directorAdapter = (PtidesPreemptiveEDFDirector) getAdapter(director);
            Map<String, String> map = directorAdapter
                    .generateAdditionalCodeFiles();
            for (String file : map.keySet()) {
                StringBuffer sb = new StringBuffer();
                if (file.contains(".")) {
                    fileExtension = file.substring(file.indexOf("."),
                            file.length());
                    _writeCodeFileName(sb.append(map.get(file)), file, true,
                            false);
                } else {
                    fileExtension = "." + file;
                    _writeCode(sb.append(map.get(file)));
                }
            }
            //        } else if (director instanceof ptolemy.domains.ptides.kernel.PtidesTopLevelDirector) {
            //            // If the PtidyOSCodeGenerator is used on the top level, then one assembly file
            //            // should be generated for each platform.
            //            for (Actor actor : (List<Actor>) ((TypedCompositeActor) getContainer())
            //                    .deepEntityList()) {
            //                Director insideDirector = actor.getDirector();
            //                if (insideDirector instanceof ptolemy.domains.ptides.kernel.PtidesBasicDirector) {
            //                    directorAdapter = (PtidesPreemptiveEDFDirector) getAdapter(director);
            //                    Map<String, String> map = directorAdapter.generateAdditionalCodeFiles();
            //                    for (String file : map.keySet()) {
            //                        fileExtension = "." + file;
            //                        StringBuffer sb = new StringBuffer();
            //                        _writeCode(sb.append(map.get(file)));
            //                    }
            //                }
            //            }
        } else {
            throw new IllegalActionException(director,
                    "This PtidyOS code generator should be used "
                            + "with a Ptides director.");
        }
    }

    /**
     * Return the name of the output file.
     * @return The output file name.
     * @exception IllegalActionException If there is problem resolving
     *  the string value of the generatorPackage parameter.
     */
    @Override
    protected String _getOutputFilename() throws IllegalActionException {
        String extension = fileExtension;

        return _sanitizedModelName + extension;
    }

    @Override
    protected String _generateIncludeFiles() throws IllegalActionException {
        return "";
    }

    /** Overwrite the base class, and use a method from the Ptides director
     *  to determine whether this is top level. Test if the containing actor
     *  is in the top level.
     *  @return true if the containing actor is in the top level.
     *  @exception IllegalActionException If the director is not a PtidesDirector.
     */
    @Override
    protected boolean _isTopLevel() throws IllegalActionException {
        Director director = ((Actor) getContainer()).getDirector();
        if (!(director instanceof ptolemy.domains.ptides.kernel.PtidesDirector)) {
            throw new IllegalActionException(director,
                    "PtidyOSCodeGenerator can only "
                            + "work with Ptides directors.");
        }
        return !director.isEmbedded();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The index of the list of code files to generate code for. 0 refers
     * to the c file, and 1 refers to the startup .S code file.
     */
    //protected int _generateFile;

    protected String fileExtension;
}
