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

public class LinkedProjectFileSet extends AbstractFileSet implements ResourceCollection {

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

	private String projectPaths;
	
	public String getProjectPaths() {
		return projectPaths;
	}
	
	public void setProjectPaths(String projectPaths) {
		this.projectPaths = projectPaths;
	}
	
	@Override
	public boolean isFilesystemOnly() {
		return true;
	}

	private Collection<Resource> resources = null;
	
	/*
	<link>
		<name>linked-src/ptolemy/actor/lib/AbsoluteValue.java</name>
		<type>1</type>
		<locationURI>PT_LOC/ptolemy/actor/lib/AbsoluteValue.java</locationURI>
	</link>
	*/

	private String sourceDirSuffix = "/";
	
	public String getSourceDirSuffix() {
		return sourceDirSuffix;
	}

	public void setSourceDirSuffix(String sourceDirSuffix) {
		this.sourceDirSuffix = sourceDirSuffix;
	}

	private boolean echo = false;

	public boolean isEcho() {
		return echo;
	}

	public void setEcho(boolean echo) {
		this.echo = echo;
	}

	private void updateResources() {
		resources = new ArrayList<Resource>();
		String[] projectPaths = getProjectPaths().split(";");
		for (int i = 0; i < projectPaths.length; i++) {
			String projectPathsPrefix = getProjectPathsPrefix();
			String projectPath = projectPaths[i].trim();
			addLinkedProjectFiles(projectPathsPrefix + (projectPathsPrefix.endsWith("/") || projectPath.startsWith("/") ? "" : "/") + projectPath);
		}
	}
	
	private void addLinkedProjectFiles(String projectPath) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(projectPath + File.separator + ".project"));
			String line = null, tagContent = null, name = null, locationURI = null;
			while ((line = reader.readLine()) != null) {
				if ((tagContent = tagContent(line, "name")) != null) {
					String suffix = getSourceDirSuffix();
					int pos = tagContent.indexOf(suffix);
					if (pos >= 0) {
						name = tagContent.substring(pos + suffix.length());
					}
				} else if ((tagContent = tagContent(line, "locationURI")) != null) {
					locationURI = tagContent.replace("PT_LOC", getPtPath()).replace("//", sourceDirSuffix);
				}
				if (name != null && locationURI != null && locationURI.endsWith(name)) {
					File baseDir = new File(getProject().getBaseDir() + sourceDirSuffix + locationURI.substring(0, locationURI.length() - name.length()));
					if (isEcho()) {
						System.out.println(baseDir + " => " + name);
					}
					resources.add(new FileResource(baseDir, name));
					name = null;
					locationURI = null;
				}
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
	
	private String tagContent(String line, String tagName) {
		int pos1 = line.indexOf("<") + 1, pos2 = line.indexOf(">", pos1);
		if (pos1 <= 0 || pos2 < 0) {
			return null;
		}
		if (! tagName.equals(line.substring(pos1, pos2))) {
			return null;
		}
		int start = pos2 + 1, end = line.indexOf("</", start);
		pos2 = line.indexOf(">", end + 2);
		if (end < 0 || pos2 < 0) {
			return null;
		}
		if (! tagName.equals(line.substring(end + 2, pos2))) {
			return null;
		}
		return line.substring(start, end);
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
