/* A declaration of a Java package.

Copyright (c) 1998-2000  The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)

*/

package ptolemy.lang.java;

import ptolemy.lang.*;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// PackageDecl
/** A declaration of a Java package.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay
@version $Id$
 */
public class PackageDecl extends JavaDecl
    implements JavaStaticSemanticConstants {

    public PackageDecl(String name, JavaDecl container) {
        super(name, CG_PACKAGE);
        _container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Re-override equals() from Decl so that equality is defined as being the
     *  same object. This is necessary to ensure that a Decl named z for x.y.z
     *  does not equal another Decl named z for x.z.
     *  @return true if the objects are the same object.
     */
    public boolean equals(Object obj) {
        return (this == obj);
    }

    public final boolean hasContainer() {
        return true;
    }

    public final JavaDecl getContainer() {
        return _container;
    }

    public final void setContainer(JavaDecl container) {
        _container = container;
    }

    public final Environ getEnviron() {
	//System.out.println("PackageDecl.getEnviron()" + getName());
        if (_environ == null) {
            _initEnviron();
        }
        return _environ;
    }

    public final void setEnviron(Environ environ) {
        _environ = environ;
    }

    public final boolean hasEnviron() { return true; }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected JavaDecl  _container;

    protected Environ _environ = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    // Initialize the environ by adding declarations for the package.
    private void _initEnviron() {
	/* SDFCodeGeneratorClassFactory.createPtolemyTypeIdentifier()
	 * ptolemy.codegen.PtolemyTypeIdentifier has a static section
         * that calls
	 * ptolemy.lang.java.StaticResolution.loadFile().
	 * StaticResolution has a static section that calls
	 * StaticResolution.importPackage on java.lang.
	 * StaticResolution.importPackage calls
	 * StaticResolution.SYSTEM_PACKAGE.getEnviron(), which calls
	 * PackageDecl.getEnviron(), which calls this method
	 * 
	 * The way to see this is to edit ../ApplicationUtility
	 * and set enableTrace to true and the recompile.
	 */

        //System.out.println("PackageDecl._initEnviron("
				 + _container + ")");

        boolean empty = true;

        if (_container == null) {
            //System.out.println("_initEnviron : no container");
            _environ = new Environ(null);
        } else {
            //System.out.println("_initEnviron : has container");
            _environ = new Environ(_container.getEnviron());
        }

        // Use the contents of the system jar file to get java.* etc. files 
        if (fullName().equals("") ||
                SearchPath.systemPackageSet.contains(fullName('.'))) {
            _initEnvironSystemPackages(SearchPath.systemClassSet,
                    SearchPath.systemPackageSet);
        }

        if (SearchPath.systemPackageSet.contains(fullName('.'))) {
                return;
        }

	System.out.println("PackageDecl._initEnviron(): " +
               "Now processing " + fullName('.'));
        // Use reflection to get at the Ptolemy Core packages.
        if (SearchPath.ptolemyCorePackageSet.contains(fullName('.'))) {
            _initEnvironSystemPackages(SearchPath.ptolemyCoreClassSet,
                    SearchPath.ptolemyCorePackageSet);

            if (fullName('.').equals("ptolemy.data")) {
                // ptolemy.data and ptolemy.data.type are part of the 
                // ptolemy II core, so we can use reflection, but
                // ptolemy.data.expr is not part of the core, so
                // we need to add it by hand so that we can parse
                // the .java files and get the bodies in to the AST.
                //System.out.println("PackageDecl._initEnviron" +
                // "Packages(): saw ptolemy/data ");
                _environ.add(new PackageDecl("expr", this));
            }
            return;
        }

        SearchPath paths = _pickLibrary(this);

        String subdir = fullName(File.separatorChar);

        if (subdir.length() > 0) {
            subdir = subdir + File.separatorChar;
        }

        //System.out.println("PackageDecl: subdir = " + subdir);
	//System.out.println("PackageDecl: found " + paths.size() +
				 " class paths" + paths.toString());

        for (int i = 0; i < paths.size(); i++) {
            String path = (String) paths.get(i);

            //System.out.println("path = " + path);

            String dirName = path + subdir;

            //System.out.println("dirName = " + dirName);

            File dir = new File(dirName);

            if (dir.isDirectory()) {

                String[] nameList = dir.list();

                //System.out.println("isDirectory = true, length = " + nameList.length);

                for (int j = 0; j < nameList.length; j++) {
                    //System.out.println("iterating over names, j = " + j);

                    String name = nameList[j];
                    int length = name.length();
                    String className = null;

                    if ((length > 5) && name.substring(length - 5).equals(".java")) {
                        className = name.substring(0, length - 5);
                    } else if ((length > 6) && name.substring(length - 6).equals(".jskel")) {
                        className = name.substring(0, length - 6);
                    }

                    if (className != null) {

                        // make sure we don't create 2 class decls if there are two files
                        // with the same base name, but with different extensions.
                        if (_environ.lookupProper(className, CG_USERTYPE) == null) {

                            //System.out.println("adding class/interface " +
                            className + " from " + dirName);

                            _environ.add(new ClassDecl(className, this));

                            empty = false;

                            //System.out.println(
                             getName() + " : found source in " + dirName + name);
                        }

                    } else {
                        String fullname = dirName + name;

                        File fs = new File(fullname);

                        if (fs.isDirectory()) {
                            _environ.add(new PackageDecl(name, this));
	                    empty = false;
                            //System.out.println(fullName() + " " +
			    // getName() + " : found subpackage in " +
                            // fullname +
			    //		       " Adding " + name);
                        }

                    } // className != null
                } // for (int j = 0; j < nameList.length; j++)
            } // if (dir.isDirectory())
        } // for (int i = 0; i < paths.size(); i++)

        if (empty && (this != StaticResolution.UNNAMED_PACKAGE)) {
            System.err.println("Warning: " +
                    "unable to find any sources or subpackages for " +
                    getName());
        }
    }

    // Initialize the Environ by loading declarations for the system packages
    // or ptolemy core packages.
    private void _initEnvironSystemPackages(Set classSet, Set packageSet) {
        String packageName = fullName('.');
        //System.out.println("PackageDecl._initEnvironSystemPackages(): " +
	//	   "loading " + packageName + " _container:" + _container);

        Iterator classes = classSet.iterator();
        while (classes.hasNext()) {
            String className = (String) classes.next();
            if (className.startsWith(packageName)) {
                String systemPackageName =
                    StringManip.partBeforeLast(className, '.');
                if (systemPackageName.equals(packageName)) {
                    if (_environ.lookupProper(className, CG_USERTYPE) == null) {
                String shortClassName =
                    className.substring(packageName.length() + 1);
                //System.out.println("PackageDecl._initEnviron" +
                //                "SystemPackages():"+
                //                shortClassName);

                        _environ.add(new ClassDecl(shortClassName, this));
                    }
                }
            }
        }

        // Add the sub packages in this package
        Iterator packages = packageSet.iterator();
        while(packages.hasNext()) {
            String systemPackageName = (String) packages.next();
            // Add the package 
            // if it is a subpackage of packageName, _or_ 
            // if the packageName is "" and the
            // systemPackage does not contain a .
            //
            // We need to check to see if the string contains
	    // a . because 
	    // if packageName == java and systemPackageName == javax
            // then partBeforeLast will fail. 
            if (systemPackageName.startsWith(packageName) &&
                    systemPackageName.indexOf('.') != -1 &&
                    StringManip.partBeforeLast(systemPackageName,
                            '.').equals(packageName)) {
                // Remove the package name and add the subpackage.
                // For example if we are in java, then add lang instead
                // of java/lang
                String shortSystemPackageName =
                    systemPackageName.substring(packageName.length() + 1);
                //System.out.println("PackageDecl._initEnvironSystem" +
		//		   "Packages(): adding package: " + shortSystemPackageName);
                _environ.add(new PackageDecl(shortSystemPackageName, this));
            } else {
                if (packageName.equals("") && 
                        systemPackageName.indexOf('.') == -1 &&
                        !systemPackageName.equals("META-INF")
                    ) {
                    //System.out.println("PackageDecl._initEnvironSystem" +
                    // "Packages(): adding toplevel package: " +
                    // systemPackageName);
                _environ.add(new PackageDecl(systemPackageName, this));
                }
            }
        }
    }
}

