package org.ptolemy.antext;

import java.util.Collection;

import org.apache.tools.ant.types.Resource;

public interface FileSetFormat {

	public boolean supports(String projectPath, String projectName);
	public void addProjectFiles(String projectPath, String projectName, Collection<Resource> resources, PtProjectFileSet fileSet);

}
