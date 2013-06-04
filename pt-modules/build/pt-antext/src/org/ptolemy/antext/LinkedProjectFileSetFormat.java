package org.ptolemy.antext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;

public class LinkedProjectFileSetFormat implements FileSetFormat {

	/*
	<link>
		<name>linked-src/ptolemy/actor/lib/AbsoluteValue.java</name>
		<type>1</type>
		<locationURI>PT_LOC/ptolemy/actor/lib/AbsoluteValue.java</locationURI>
	</link>
	*/

	@Override
	public File getProjectFileSetFile(String projectPath, String projectName) {
		return new File(projectPath + File.separator + ".project");
	}
	
	@Override
	public void addProjectFiles(String projectPath, String projectName, Collection<Resource> resources, PtProjectFileSet fileSet) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(getProjectFileSetFile(projectPath, projectName)));
			String line = null, tagContent = null, name = null, locationURI = null;
			while ((line = reader.readLine()) != null) {
				String sourceDirSuffix = "src/";
				if ((tagContent = tagContent(line, "name")) != null) {
					int pos = tagContent.indexOf(sourceDirSuffix);
					if (pos >= 0) {
						name = tagContent.substring(pos + sourceDirSuffix.length());
					}
				} else if ((tagContent = tagContent(line, "locationURI")) != null) {
					locationURI = tagContent.replace("PT_LOC", fileSet.getPtPath()).replace("//", sourceDirSuffix);
				}
				if (name != null && locationURI != null && locationURI.endsWith(name)) {
					File baseDir = new File(fileSet.getProject().getBaseDir() + File.separator + locationURI.substring(0, locationURI.length() - name.length()));
					if (fileSet.isEcho()) {
						System.out.println(resources.size() + ": " + baseDir + "+" + name);
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
}
