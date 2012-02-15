/* Instantiate a Functional Mock-up Unit (FMU).

 Copyright (c) 2011 The Regents of the University of California.
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// FMUImport

/**
 Instantiate a Functional Mock-up Unit (FMU).

 @author Christopher Brooks, Michael Wetter, Edward A. Lee, 
 @version $Id$
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class FMUImport extends TypedAtomicActor {
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
     *  and any necessary shared libraries.
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
            _updateParameters();
        }

        super.attributeChanged(attribute);
    }
    /** 
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }

    /** Unzip a file.
     *  Based on http://java.sun.com/developer/technicalArticles/Programming/compression/
     *  @param zipFilename  The file to be unzipped.
     *  @return a list of canonical paths to the files created
     *  @exception IOException if the file cannot be opened, if there are problems reading
     *  the zip file or if there are problems creating the files or directories.
     */
    private List _unzip(String zipFileName) throws IOException {
        BufferedOutputStream destination = null;
        FileInputStream fileInputStream = new FileInputStream(zipFileName);
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
        ZipEntry entry;
        final int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        // FIXME: maybe put this in the tmp directory?
        String topDirectory = zipFileName.substring(0, zipFileName.length() - 4);
        List files = new LinkedList();
        try {
            while((entry = zipInputStream.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry);
                String entryName = entry.getName();
                File destinationFile = new File(topDirectory, entryName);
                File destinationParent = destinationFile.getParentFile();
                // If the directory does not exist, create it.
                if (!destinationParent.isDirectory()
                    && !destinationParent.mkdirs()) {
                    throw new IOException("Failed to create \""
                            + destinationParent + "\".");
                }
                // If the entry is not a directory, then write the file.
                if (!entry.isDirectory()) {
                    // Write the files to the disk.
                    FileOutputStream fos = new FileOutputStream(destinationFile); 
                    destination = new BufferedOutputStream(fos, BUFFER);
                    int count;
                    while ((count = zipInputStream.read(data, 0, BUFFER)) 
                            != -1) {
                        destination.write(data, 0, count);
                    }
                    destination.flush();
                    destination.close();
                    files.add(destinationFile.getCanonicalPath());
                }
            }
        } finally {
            if (zipInputStream != null) {
                zipInputStream.close();
            }
        }
        return files;
    }

    /** Update the parameters listed in the modelDescription.xml file
     *  contained in the zipped file named by the <i>fmuFile</i>
     *  parameter
     *  @exception IllegalActionException If the file named by the
     *  <i>fmuFile<i> parameter cannot be unzipped or if there
     *  is a problem deleting any preexisting parameters or
     *  creating new parameters.
     */
    private void _updateParameters() throws IllegalActionException {
        // Unzip the fmuFile.  We probably need to do this
        // because we will need to load the shared library later.
        String fmuFileName = null;
        List files = null;
        try {
            fmuFileName = fmuFile.asFile().getCanonicalPath();
            files = _unzip(fmuFileName);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to unzip \"" + fmuFileName + "\".");
        }
        System.out.println("FMUImport: created " + files.size() + " files.");
    }
}
