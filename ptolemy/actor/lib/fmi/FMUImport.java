/* Instantiate a Functional Mock-up Unit (FMU).

   Copyright (c) 2011-2012 The Regents of the University of California.
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
package ptolemy.actor.lib.fmi;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIRealType;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMUFile;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FMUImport

/**
 * Invoke a Functional Mock-up Interface (FMI) 1.0 Model Exchange 
 * Functional Mock-up Unit (FMU).
 * 
 * <p>Read in a <code>.fmu</code> file named by the 
 * <i>fmuFile</i> parameter.  The <code>.fmu</code> file is a zipped
 * file that contains a file named <code>modelDescription.xml</code>
 * that describes the ports and parameters that are created.
 * At run time, method calls are made to C functions that are
 * included in shared libraries included in the <code>.fmu</code>
 * file.</p>
 * 
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 * 
 * @author Christopher Brooks, Michael Wetter, Edward A. Lee, 
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUImport extends TypedCompositeActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FMUImport(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        fmuFile = new FileParameter(this, "fmuFile");
        fmuFile.setExpression("fmuImport.fmu");
    }

    /** The Functional Mock-up Unit (FMU) file.
     *  The FMU file is a zip file that contains a file named "modelDescription.xml"
     *  and any necessary shared libraries.  The file is read when this
     *  actor is instantiated or when the file name changes.  The initial default
     *  value is "fmuImport.fmu".
     */
    public FileParameter fmuFile;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>fmuFile</i>, then unzip
     *  the file and load in the .xml file, creating and deleting parameters
     *  as necessary.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *  is <i>fmuFile</i> and the file cannot be opened or there
     *  is a problem creating or destroying the parameters
     *  listed in thile.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fmuFile) {
            try {
                _updateParameters();
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e, "Name duplication");
            }
        }

        super.attributeChanged(attribute);
    }

    /**
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }

    /** Update the parameters listed in the modelDescription.xml file
     *  contained in the zipped file named by the <i>fmuFile</i>
     *  parameter
     *  @exception IllegalActionException If the file named by the
     *  <i>fmuFile<i> parameter cannot be unzipped or if there
     *  is a problem deleting any pre=existing parameters or
     *  creating new parameters.
     *  @exception NameDuplicationException If a paramater to be created
     *  has the same name as a pre-existing parameter.
     */
    private void _updateParameters()
            throws IllegalActionException, NameDuplicationException {

        // Unzip the fmuFile.  We probably need to do this
        // because we will need to load the shared library later.
        String fmuFileName = null;
        try {
            // FIXME: Use URLs, not files so that we can work from JarZip files.
            
            // Only read the file if the name has changed from the last time we
            // read the file or if the modification time has changed.
            fmuFileName = fmuFile.asFile().getCanonicalPath();
            if (fmuFileName == _fmuFileName) {
                return;
            }
            _fmuFileName = fmuFileName;
            long modificationTime = new File(fmuFileName).lastModified();
            if (_fmuFileModificationTime == modificationTime) {
                return;
            }
            _fmuFileModificationTime = modificationTime;

            _fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);
            
            // Instantiate ports and parameters.
            for (FMIScalarVariable scalar : _fmiModelDescription.modelVariables) {
                if (scalar.type instanceof FMIRealType) {
                    if (scalar.variability == FMIScalarVariable.Variability.parameter) {
                        Parameter parameter = new Parameter(this, scalar.name);
                        parameter.setExpression(Double.toString(((FMIRealType)scalar.type).start));
                        // Prevent exporting this to MoML unless it has
                        // been overridden.
                        parameter.setDerivedLevel(1);
                    } {
                        // FIXME: All output ports?
                        TypedIOPort port = new TypedIOPort(this, scalar.name, false, true);
                        port.setDerivedLevel(1);
                    }
                } else {
                    throw new IllegalActionException("Don't know how to handle "
                            + scalar.type);
                }
            }
            String library = null;
            try {
                library = FMUFile.fmuSharedLibrary(_fmiModelDescription);
                System.out.println("About to load " + library);
                System.load(library);
            } catch (Throwable throwable) {
                List<String> binariesFiles = new LinkedList<String>();
                for (File file : _fmiModelDescription.files) {
                    if (file.toString().indexOf("binaries") != -1) {
                        binariesFiles.add(file.toString() + "\n");
                    }
                }
                String message = "Failed to load the \""
                        + library + "\" shared library, which was created "
                        + "by unzipping \"" +
                        fmuFile.asFile()
                        + "\". Usually, this is because the .fmu file does "
                        + "not contain a shared library for the current "
                        + "architecture.  The fmu file contained the "
                        + "following files with 'binaries' in the path:\n" 
                        + binariesFiles;
                System.out.println(this.getFullName() + ": "
                        + message + "\n Original error:\n " 
                        + throwable);
                // Note that Variable.propagate() will handle this error and
                // hide it.
                throw new IllegalActionException(this, throwable, message);
            }


        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to unzip, read in or process \"" + fmuFileName + "\".");
        }
    }

    /** The name of the fmuFile.
     *  The _fmuFileName field is set the first time we read
     *  the file named by the <i>fmuFile</i> parameter.  The
     *  file named by the <i>fmuFile</i> parameter is only read
     *  if the name has changed or if the modification time of 
     *  the file is later than the time the file was last read.
     */
    private String _fmuFileName = null;
    
    /** The modification time of the file named by the
     *  <i>fmuFile</i> parameter the last time the file was read.
     */
    private long _fmuFileModificationTime = -1;

    /** A representation of the fmiModelDescription element of a
     *  Functional Mock-up Unit (FMU) file.
     */
    FMIModelDescription _fmiModelDescription;
}
