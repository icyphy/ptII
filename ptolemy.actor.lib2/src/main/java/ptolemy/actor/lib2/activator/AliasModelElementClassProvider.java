/* A simple provider that gets a list of classes to be provided in its constructor.

Copyright (c) 2015 The Regents of the University of California; iSencia Belgium NV.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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
package ptolemy.actor.lib2.activator;

import java.util.Map;

import org.ptolemy.classloading.ModelElementClassProvider;
import org.ptolemy.commons.VersionSpecification;

import ptolemy.kernel.util.NamedObj;

/**
 * A simple provider that gets a list of classes to be provided in its constructor.

   @author erwinDL
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Red (erwinDL)
   @Pt.AcceptedRating Red (reviewmoderator)
 */
public class AliasModelElementClassProvider implements ModelElementClassProvider {

  /**
   * Creates a provider that does not care about class versions,
   * i.e. it will only check on class names to check if it can provide a requested class.
   *
   * @param knownClasses
   */
  public AliasModelElementClassProvider(Map<String,Class<? extends NamedObj>> knownClasses) {
    this(null, knownClasses);
  }

  /**
   * Creates a provider that cares about class versions,
   * i.e. it will check on class names and on the requested version to check if it can provide a requested class.
   *
   * @param version if null, the provider will not care about versions
   * @param knownClasses
   */
  public AliasModelElementClassProvider(VersionSpecification version, Map<String,Class<? extends NamedObj>> knownClasses) {
    this._knownClasses = knownClasses;
    this._versionSpec = version;
  }

  public Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    if (_versionSpec != null && _versionSpec.compareTo(versionSpec)<0) {
      throw new ClassNotFoundException(className + " " + versionSpec);
    } else {
      for (Map.Entry<String, Class<? extends NamedObj>> actorEntry : _knownClasses.entrySet()) {
        if (actorEntry.getKey().equals(className)) {
          return actorEntry.getValue();
        }
      }
      throw new ClassNotFoundException(className);
    }
  }

  // private stuff

  private VersionSpecification _versionSpec;
  private Map<String,Class<? extends NamedObj>> _knownClasses;
}
