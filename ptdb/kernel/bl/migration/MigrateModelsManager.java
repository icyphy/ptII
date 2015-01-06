/*
@Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptdb.kernel.bl.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import ptdb.common.dto.XMLDBModel;
import ptdb.kernel.bl.save.SaveModelManager;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// MigrateModelsManager

/**
 *
 * Handle the migration of models from the file system to the database at the
 * business layer.
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class MigrateModelsManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Migrate models from the file system stored in the given path to the
     * database.
     *
     * @param directoryPath The path on the file system where the models exist.
     * @param migrateFilesInSubDirectories A boolean that indicates if the
     * migration should consider files in sub-directories.
     * @param checkContent A boolean indicating if the method should check the
     * file content before creating a model and store it in the database or not.
     * @return A string representing the path to the CSV file the contains the
     * results of the migration.
     * @exception IOException Thrown if there is an error reading or writing
     * files.
     */
    public String migrateModels(String directoryPath,
            boolean migrateFilesInSubDirectories, boolean checkContent)
                    throws IOException {

        //check if the path provided exists.
        File directoryFile = new File(directoryPath);

        if (directoryFile.exists() == false) {
            throw new IOException("Directory: " + directoryPath
                    + " does not exist.");
        }

        String csvFilePath = directoryPath
                + System.getProperty("file.separator") + "migrationResults.csv";

        // Check if the application has write access to the csv file path.
        try {

            File csvFile = new File(csvFilePath);
            if (!csvFile.createNewFile()) {
                throw new IOException("Failed to create " + csvFilePath);
            }

        } catch (IOException e) {

            csvFilePath = StringUtilities.preferencesDirectory()
                    + "migrationResults.csv";
        }

        _csvFileWriter = new FileWriter(csvFilePath);

        //write the header for the csv file.
        _csvFileWriter
        .write("Model Name,File Path,Migration Status,Error Messages"
                + System.getProperty("line.separator"));

        try {

            _readFiles(directoryFile, directoryFile,
                    migrateFilesInSubDirectories, checkContent);

        } finally {

            _csvFileWriter.flush();

            _csvFileWriter.close();
        }

        return csvFilePath;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Reads the models in the given directory and store them in the database.
     *
     * @param directory A file that represents the directory that contains the
     * models.
     * @param parentDirectory A file that represents the first directory passed.
     * @param readSubDirectories A boolean indicating if the method should
     * consider reading sub-directory files or not.
     * @param checkContent A boolean indicating if the method should check the
     * file content before creating a model and store it in the database or not.
     * @exception IOException Thrown if there is an error while reading or
     * writing files.
     */
    private void _readFiles(File directory, File parentDirectory,
            boolean readSubDirectories, boolean checkContent)
                    throws IOException {

        // If the path sent is a file, try to create a model in the database out of it.
        if (directory.isFile()) {

            String modelName = "";

            // Only files with .xml extension will be considered.

            if (directory.getName().endsWith(".xml")) {

                modelName = directory.getName().substring(0,
                        directory.getName().indexOf(".xml"));

                String fileContent = _getContent(directory);

                if (checkContent == false
                        || (/*checkContent &&*/_checkFileContent(fileContent))) {

                    _createDBModel(modelName, fileContent,
                            directory.getAbsolutePath());

                } else {

                    _csvFileWriter.write(modelName + ","
                            + directory.getAbsolutePath() + ",Failed,"
                            + "The content of the file"
                            + " was not a proper Ptolemy model content."
                            + System.getProperty("line.separator"));

                }
            }

        } else if (directory.isDirectory()
                && readSubDirectories
                || directory.isDirectory()
                && !readSubDirectories
                && directory.getAbsolutePath().equalsIgnoreCase(
                        parentDirectory.getAbsolutePath())) {
            // If the path is a directory, get the list of files and call
            // this method recursively on each of the files.

            File[] listOfFiles = directory.listFiles();

            if (listOfFiles != null) {

                for (File listOfFile : listOfFiles) {

                    _readFiles(listOfFile, parentDirectory, readSubDirectories,
                            checkContent);

                }
            }
        }
    }

    /**
     * Read the content of a given file and return it to the caller as a string.
     * @param file The file that we need to read its content.
     * @return A string representation of the content of the model.
     * @exception IOException Thrown if there is an error reading the content of
     * the file.
     */
    private String _getContent(File file) throws IOException {

        StringBuilder contents = new StringBuilder();

        BufferedReader input = new BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(file), java.nio.charset.Charset.defaultCharset()));

        try {

            String line = null;

            while ((line = input.readLine()) != null) {

                contents.append(line);

                contents.append(System.getProperty("line.separator"));

            }
        } finally {

            input.close();

        }

        return contents.toString();

    }

    /**
     * create a new model in the database based on the parameters passed.
     *
     * @param modelName The name of the model to be created.
     * @param modelContent The content of the model to be created.
     * @param filePath The absolute file path to the model on the file system.
     * @exception IOException Thrown if there is an error writing the result to
     * the CSV file.
     */
    private void _createDBModel(String modelName, String modelContent,
            String filePath) throws IOException {

        XMLDBModel xmlDBModel = new XMLDBModel(modelName);

        xmlDBModel.setModel(modelContent);

        xmlDBModel.setIsNew(true);

        SaveModelManager saveModelManager = new SaveModelManager();

        try {

            saveModelManager.save(xmlDBModel);

            _csvFileWriter.write(modelName + "," + filePath + ",Successful, "
                    + System.getProperty("line.separator"));

        } catch (Exception e) {
            _csvFileWriter.write(modelName + "," + filePath + ",Failed,"
                    + e.getMessage() + System.getProperty("line.separator"));
        }
    }

    /**
     * Check if the file content is a proper Ptolemy Model content.
     * @param fileContent The content of the file.
     * @return boolean indicating whether the file content is a proper
     * Ptolemy model or not.
     */
    private boolean _checkFileContent(String fileContent) {

        boolean isPtolemyModel = false;

        if (fileContent.lastIndexOf("</") >= 0) {
            String lastTag = fileContent.substring(fileContent
                    .lastIndexOf("</"));

            String lowerCaseLastTag = lastTag.toLowerCase(Locale.getDefault());
            if (lowerCaseLastTag.contains("entity")
                    || lowerCaseLastTag.contains("class")) {
                isPtolemyModel = true;
            }
        }

        return isPtolemyModel;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /** File writer to handle writing the migration result to CSV file. */
    private FileWriter _csvFileWriter;

}
