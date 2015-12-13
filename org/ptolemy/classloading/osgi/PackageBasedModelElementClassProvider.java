package org.ptolemy.classloading.osgi;

import org.ptolemy.classloading.ModelElementClassProvider;
import org.ptolemy.commons.VersionSpecification;

import ptolemy.kernel.util.NamedObj;

public class PackageBasedModelElementClassProvider implements ModelElementClassProvider {
  
  private String[] packageNames;
  private ClassLoader classLoader;

  public PackageBasedModelElementClassProvider(ClassLoader classLoader, String... packageNames) {
    this.classLoader = classLoader;
    this.packageNames = packageNames;
  }

  @Override
  public Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    if(packageNames!=null) {
      boolean packageNameMatch = false;
      for(String packageName : packageNames) {
        packageNameMatch = className.startsWith(packageName);
        if(packageNameMatch) break;
      }
      if(!packageNameMatch) {
        throw new ClassNotFoundException();
      }
    }
    return (Class<? extends NamedObj>) this.classLoader.loadClass(className);
  }

}
