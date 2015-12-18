package ptolemy.actor.lib.gui.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.ptolemy.classloading.ModelElementClassProvider;
import org.ptolemy.classloading.osgi.DefaultModelElementClassProvider;
import org.ptolemy.commons.ThreeDigitVersionSpecification;
import org.ptolemy.commons.VersionSpecification;

import ptolemy.actor.lib.gui.Display;

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

    _apSvcReg = context.registerService(ModelElementClassProvider.class.getName(),
        new DefaultModelElementClassProvider(providerVersion, Display.class),
        null);
  }

  public void stop(BundleContext context) throws Exception {
    _apSvcReg.unregister();
  }

  // private stuff
  /** The svc registration for the actor provider */
  private ServiceRegistration<?> _apSvcReg;
}