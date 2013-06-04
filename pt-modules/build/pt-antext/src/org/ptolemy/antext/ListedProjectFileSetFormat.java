package org.ptolemy.antext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;

public class ListedProjectFileSetFormat implements FileSetFormat {

	private String fileListPath;
	
	public String getFileListPath() {
		return fileListPath;
	}
	
	public void setFileListPath(String fileListPath) {
		this.fileListPath = fileListPath;
	}
	
	/*
	either a comment starting with anything except a / or a filename (starting with /) 
	*/

	@Override
	public File getProjectFileSetFile(String projectPath, String projectName) {
		return new File(projectPath + File.separator + "pt-jar.files");
	}

	@Override
	public void addProjectFiles(String projectPath, String projectName, Collection<Resource> resources, PtProjectFileSet fileSet) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(getProjectFileSetFile(projectPath, projectName)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (! line.startsWith("/")) {
					continue;
				}
				File baseDir = new File(fileSet.getProject().getBaseDir() + File.separator + fileSet.getPtPath());
				String name = line.substring(1);
				if (fileSet.isEcho()) {
					System.out.println(resources.size() + ": " + baseDir + "+" + name);
				}
				resources.add(new FileResource(baseDir, name));
			}
		} catch (IOException e) {
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
