/* This activator registers an actor provider for the MyConst custom actor implementation.

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
package ptolemy.kernel.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.ptolemy.classloading.ModelElementClassProvider;
import org.ptolemy.classloading.osgi.PackageBasedModelElementClassProvider;
import org.ptolemy.commons.ThreeDigitVersionSpecification;
import org.ptolemy.commons.VersionSpecification;

/**
 * This activator registers an actor provider for the ptolemy actors and other model elements.
 *
 * @author erwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (erwinDL)
 * @Pt.AcceptedRating Red (reviewmoderator)
 */
public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {

	  // FIXME figure out a more compact way to create a version-aware provider,
	  // that uses the bundle version but is not too dependent on OSGi APIs itself.
	  Version bundleVersion = context.getBundle().getVersion();
	  VersionSpecification providerVersion = new ThreeDigitVersionSpecification(
	      bundleVersion.getMajor(),
	      bundleVersion.getMinor(),
	      bundleVersion.getMicro(),
	      bundleVersion.getQualifier());

//    _apSvcReg = context.registerService(ModelElementClassProvider.class.getName(),
//        new DefaultModelElementClassProvider(providerVersion, Const.class, Counter.class),
//        null);

    _apSvcReg = context.registerService(ModelElementClassProvider.class.getName(),
        new PackageBasedModelElementClassProvider(this.getClass().getClassLoader(),
            "ptolemy.actor",
            "ptolemy.data",
            "ptolemy.kernel",
            "ptolemy.math",
            "ptolemy.util"
            ),
        null);

	}

	public void stop(BundleContext context) throws Exception {
	  _apSvcReg.unregister();
	}

	// private stuff
  /** The svc registration for the actor provider */
	private ServiceRegistration<?> _apSvcReg;
}
