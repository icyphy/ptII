package org.ptolemy.antext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;

public class PtProjectFileSet extends AbstractFileSet implements ResourceCollection {

	private String ptPath;

	public String getPtPath() {
		return ptPath;
	}

	public void setPtPath(String ptPath) {
		this.ptPath = ptPath;
	}

	private String projectPathsPrefix;
	
	public String getProjectPathsPrefix() {
		return projectPathsPrefix;
	}
	
	public void setProjectPathsPrefix(String projectPathsPrefix) {
		this.projectPathsPrefix = projectPathsPrefix;
	}

	private String projectNames;
	
	public String getProjectNames() {
		return projectNames;
	}
	
	public void setProjectNames(String projectNames) {
		this.projectNames = projectNames;
	}

	@Override
	public boolean isFilesystemOnly() {
		return true;
	}

	private Collection<Resource> resources = null;

	private boolean echo = false;

	public boolean isEcho() {
		return echo;
	}

	public void setEcho(boolean echo) {
		this.echo = echo;
	}

	private FileSetFormat[] fileSetFormats = {
			new ListedProjectFileSetFormat(), new LinkedProjectFileSetFormat()
	};
	
	protected void updateResources() {
		resources = new ArrayList<Resource>();
		String[] projectPaths = getProjectNames().split(";");
		for (int i = 0; i < projectPaths.length; i++) {
			String projectPathsPrefix = getProjectPathsPrefix();
			String projectName = projectPaths[i].trim();
			String projectPath = getProject().getBaseDir() + File.separator + projectPathsPrefix + (projectPathsPrefix.endsWith("/") || projectName.startsWith("/") ? "" : "/") + projectName;
			for (int j = 0; j < fileSetFormats.length; j++) {
				if (isEcho()) {
					System.out.println("# Trying " + fileSetFormats[j] + " on " + projectName + " @ " + projectPath);
				}
				if (fileSetFormats[j].supports(projectPath, projectName)) {
					if (isEcho()) {
						System.out.println("# files for " + projectName + " @ " + projectPath);
					}
					fileSetFormats[j].addProjectFiles(projectPath, projectName, resources, this);
					break;
				}
			}
		}
	}
	
	protected Collection<Resource> getResources() {
		if (resources == null) {
			updateResources();
		}
		return resources;
	}

	@Override
	public Iterator iterator() {
		return getResources().iterator();
	}

	@Override
	public int size() {
		return getResources().size();
	}
}
