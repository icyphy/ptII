/* This is a trial "builder" class to generate .project files from pt-jar.files contents

 Copyright (c) 2014 The Regents of the University of California.

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
package org.ptolemy.modulebuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

/**
 * Launch this with the names of the module projects for which the module project files should be built.
 * Run this in the normal way as Java application from your eclipse workspace.
 * The resulting project files are generated in the results folder, in sub-folders per module.
 * <p>
 * It assumes a working directory ${workspace_loc:org.ptolemy.modulebuilder}, and that the requested module projects are present in the workspace.
 * </p>
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
public class ModuleBuilderByCopying {

  public static void main(String[] projects) {
    String moduleRootPath = "../";
    String ptSrcPath = System.getProperty("org.ptolemy.src.loc", "../");
    if(projects.length==0) {
      projects = new String[] {
          "org.ptolemy.commons",
          "ptolemy.actor.lib",
          "ptolemy.actor.lib.gui",
          "ptolemy.core",
          "ptolemy.core.test",
          "ptolemy.domains.process",
          "ptolemy.domains.sdf",
          "ptolemy.gui",
          "ptolemy.moml"
          };
    }
    File ptSrcFolder = new File(ptSrcPath);
    for (String project : projects) {
      File projectFolder = new File(moduleRootPath, project);
      File ptJarFile = new File(projectFolder, "pt-jar.files");
      if (ptJarFile.exists()) {
        STGroupFile stgf = new STGroupFile("project.stg", '$', '$');
        ST template = stgf.getInstanceOf("projectFileTemplate");
        template.add("project", project);
        File projectResultsFolder = new File("results", project);
        writeProjectFile(projectResultsFolder, template.render());
        File resultSrcFolder = new File(projectResultsFolder, "original-src");
        copySrcFiles(ptSrcFolder, resultSrcFolder, ptJarFile);
      } else {
        System.err.println("No pt-jar.files found for project " + project);
      }
    }
  }

  private static void writeProjectFile(File folder, String fileContents) {
    File projectFile = new File(folder, "generated.project");
    try {
      FileUtils.writeStringToFile(projectFile, fileContents);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void copySrcFiles(File ptSrcFolder, File resultSrcFolder, File jarContentsFile) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(jarContentsFile));
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("/")) {
          continue;
        } else {
          File srcFile = new File(ptSrcFolder, line);
          if(!srcFile.exists()) {
            System.err.println("src file not found :" + srcFile);
          } else {
            FileUtils.copyFile(srcFile, new File(resultSrcFolder, line));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
        }
      }
    }
  }
}
