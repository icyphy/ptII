/* A declaration of a Java package.

Code and comments adopted from the Titanium project.

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

import java.io.File;

import ptolemy.lang.*;

/** A declaration of a Java package. Code and comments adopted from the Titanium project.
 *
 *  @author Jeff Tsay
 */
public class PackageDecl extends JavaDecl implements JavaStaticSemanticConstants {
    public PackageDecl(String name, JavaDecl container) {
        super(name, CG_PACKAGE);
        _container = container;
    }

    /** Re-override equals() from Decl so that equality is defined as being the
     *  same object. This is necessary to ensure that a Decl named z for x.y.z
     *  does not equal another Decl named z for x.z.
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
        if (_environ == null) {
           _initEnviron();
        }
        return _environ;
    }

    public final void setEnviron(Environ environ) {
        _environ = environ;
    }

    public final boolean hasEnviron() { return true; }

    protected void _initEnviron() {

        ApplicationUtility.trace("_initEnviron");

        boolean empty = true;

        if (_container == null) {
            ApplicationUtility.trace("_initEnviron : no container");
           _environ = new Environ(null);
        } else {
            ApplicationUtility.trace("_initEnviron : has container");
           _environ = new Environ(_container.getEnviron());
        }

        SearchPath paths = _pickLibrary(this);

        String subdir = fullName(File.separatorChar);

        if (subdir.length() > 0) {
           subdir = subdir + File.separatorChar;
        }

        ApplicationUtility.trace("subdir = " + subdir);
        ApplicationUtility.trace("found " + paths.size() + " class paths");

        for (int i = 0; i < paths.size(); i++) {
            String path = (String) paths.get(i);

            ApplicationUtility.trace("path = " + path);

            String dirName = path + subdir;

            ApplicationUtility.trace("dirName = " + dirName);

            File dir = new File(dirName);

            if (dir.isDirectory()) {
  
               String[] nameList = dir.list();

               ApplicationUtility.trace("isDirectory = true, length = " + nameList.length);

               for (int j = 0; j < nameList.length; j++) {
                   ApplicationUtility.trace("iterating over names, j = " + j);
                              
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
                      
                         ApplicationUtility.trace("adding class/interface " +
                          className + " from " + dirName);

                         //System.out.println("creating new class decl for " + className + " in pd");

                         _environ.add(new ClassDecl(className, this));

 	                     empty = false;
 
                        //ApplicationUtility.trace(
                        // getName() + " : found source in " + dirName + name);
                      }

  	               } else {
	                  String fullname = dirName + name;

                      File fs = new File(fullname);

                      if (fs.isDirectory()) {
             	        _environ.add(new PackageDecl(name, this));
	                    empty = false;
                        ApplicationUtility.trace(
                         getName() + " : found subpackage in " + fullname);
                      }

	               } // className != null
               } // for (int j = 0; j < nameList.length; j++)
            } // if (dir.isDirectory())
        } // for (int i = 0; i < paths.size(); i++)

        if (empty && (this != StaticResolution.UNNAMED_PACKAGE)) {
           ApplicationUtility.warn(
            "unable to find any sources or subpackages for " + getName());
        }
    }

    protected JavaDecl  _container;

    protected Environ _environ = null;
}
