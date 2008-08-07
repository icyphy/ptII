// $Id$
// The .cpp and .h files in this directory are from salma-hayek, found at
// http://software.jessies.org/terminator/
// salma-hayek is LGPL'd, see the launcher-copyright.htm file

#ifdef __CYGWIN__
#include <windows.h>
#endif

#include "DirectoryIterator.h"
#include "join.h"
#include "synchronizeWindowsEnvironment.h"

#include <jni.h>

#include <algorithm>
#include <deque>
#include <dlfcn.h>
#include <fstream>
#include <iostream>
#include <sstream>
#include <stdexcept>
#include <string>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <vector>

// PTJAVA_HOME is defined by configure. 
// Typical value for PTJAVA_HOME is c:/Program Files/Java/jdk1.5.0_06/jre
// We use PTJAVA_HOME to avoid problems if for some reason we can't
// read from the registry.
#ifndef PTJAVA_HOME
#define PTJAVA_HOME "c:/Program Files/Java/jdk1.5.0_08/jre"
#endif

const char* programName;
const char* jdkRegistryPath = "/proc/registry/HKEY_LOCAL_MACHINE/SOFTWARE/JavaSoft/Java Development Kit";
const char* jreRegistryPath = "/proc/registry/HKEY_LOCAL_MACHINE/SOFTWARE/JavaSoft/Java Runtime Environment";


struct UsageError : std::runtime_error {
  UsageError(const std::string& description)
  : std::runtime_error(description) {
  }
};

typedef std::deque<std::string> NativeArguments;

extern "C" {
  typedef jint JNICALL (*CreateJavaVM)(JavaVM**, void**, void*);
}

struct JvmLocation {
private:
  std::string jvmDirectory;
  
public:
  static std::string readFile(const std::string& path) {
    std::ifstream is(path.c_str());
    if (is.good() == false) {
      throw UsageError("Couldn't open \"" + path + "\".");
    }
    std::ostringstream contents;
    contents << is.rdbuf();
    return contents.str();
  }
  
  static std::string readRegistryFile(const std::string& path) {
    std::string contents = readFile(path);
    // Cygwin's representation of REG_SZ keys seems to include the null terminator.
    if (contents.empty() == false && contents[contents.size() - 1] == '\0') {
      return contents.substr(0, contents.size() - 1);
    }
    return contents;
  }
  
  std::string chooseVersionFromRegistry(const std::string& registryPath) const {
    std::vector<std::string> versions;
    for (DirectoryIterator it(registryPath); it.isValid(); ++ it) {
      std::string version = it->getName();
      if (version.empty() || isdigit(version[0]) == false) {
        // Avoid "CurrentVersion", "BrowserJavaVersion", or anything else Sun might think of.
        // "CurrentVersion" didn't get updated when I installed JDK-1.5.0_06 (or the two prior versions by the look of it)..
        continue;
      }
      if (version < "1.5") {
        continue;
      }
      if (version >= "1.6") {
        // Uncomment the next line to prevent usage of 1.6.
        //continue;
      }
      versions.push_back(version);
    }
    std::sort(versions.begin(), versions.end());
    if (versions.empty()) {
      throw UsageError("No suitable Java found under \"" + registryPath + "\".");
    }
    std::string version = versions.back();
    return version;
  }
  
  std::string findJvmLibraryUsingDefine() const {
    // PTJAVA_HOME is defined by configure. 
    std::string ptjava_home = PTJAVA_HOME;
    return ptjava_home + "/bin/client/jvm.dll";
  }

  std::string findJvmLibraryUsingJreRegistry() const {
    std::string version = chooseVersionFromRegistry(jreRegistryPath);
    // What should we do if this points to "client" when we want "server"?
    std::string jvmRegistryPath = std::string(jreRegistryPath) + "/" + version + "/RuntimeLib";
    return readRegistryFile(jvmRegistryPath);
  }
  
  std::string findJvmLibraryUsingJdkRegistry() const {
    std::string version = chooseVersionFromRegistry(jdkRegistryPath);
    std::string javaHome = readRegistryFile(std::string(jdkRegistryPath) + "/" + version + "/JavaHome");
    return javaHome + "/jre/bin/client/jvm.dll";
  }
  
  std::string findWin32JvmLibrary() const {
    std::ostringstream os;
    os << programName << ": ";
    os << "Couldn't find jvm.dll in the Windows registry by looking for";
    os << std::endl;
    os << jdkRegistryPath;
    os << std::endl;
    os << "and";
    os << std::endl;
    os << jreRegistryPath;
    os << std::endl;
    os << "Please install a 1.5 or newer version JRE or JDK.";
    os << std::endl;
    os << "Error messages were:";
    os << std::endl;
    try {
#ifdef TESTING_PTJAVA_HOME
        std::cout << "Testing PTJAVA_HOME, skipping findJvmLibraryUsingJdkRegistry();" << std::endl;
#else
        return findJvmLibraryUsingJdkRegistry();
#endif
    } catch (const std::exception& ex) {
      os << "  ";
      os << ex.what();
      os << std::endl;
    }
    try {
#ifdef TESTING_PTJAVA_HOME
        std::cout << "Testing PTJAVA_HOME, skipping findJvmLibraryUsingJreRegistry();" << std::endl;
#else
      return findJvmLibraryUsingJreRegistry();
#endif
    } catch (const std::exception& ex) {
      os << "  ";
      os << ex.what();
      os << std::endl;
    }
    try {
      // To test this method, compile with -DTESTING_PTJAVA_HOME
      return findJvmLibraryUsingDefine();
    } catch (const std::exception& ex) {
      os << "  ";
      os << ex.what();
      os << std::endl;
    }
    throw UsageError(os.str());
  }
  
  std::string findJvmLibraryFilename() const {
#if defined(__CYGWIN__)
    return findWin32JvmLibrary();
#else
#if defined(__MACH__)
    return "libjvm.dylib";
#endif
    // This only works on Linux if LD_LIBRARY_PATH is already set up to include something like:
    // "$JAVA_HOME/jre/lib/$ARCH/" + jvmDirectory
    // "$JAVA_HOME/jre/lib/$ARCH"
    // "$JAVA_HOME/jre/../lib/$ARCH"
    // Where $ARCH is "i386" rather than `arch`.
    return "libjvm.so";
#endif
  }
  
  void setClientClass() {
    jvmDirectory = "client";
  }
  void setServerClass() {
    jvmDirectory = "server";
  }
  
  JvmLocation() {
    setClientClass();
  }
};

struct JavaInvocation {
private:
  JavaVM* vm;
  JNIEnv* env;
  
private:
  void checkException() {
      if (env->ExceptionOccurred()) {
          std::cout << "Exception Occurred:" << std::endl;
          env->ExceptionDescribe();
          env->ExceptionClear();
      }
  }
  static CreateJavaVM findCreateJavaVM(const char* sharedLibraryFilename) {
    void* sharedLibraryHandle = dlopen(sharedLibraryFilename, RTLD_LAZY);
    if (sharedLibraryHandle == 0) {
      std::ostringstream os;
      os << "dlopen(\"" << sharedLibraryFilename << "\") failed with " << dlerror() << ".";
      throw UsageError(os.str());
    }
    // Work around:
    // warning: ISO C++ forbids casting between pointer-to-function and pointer-to-object
    CreateJavaVM createJavaVM = reinterpret_cast<CreateJavaVM> (reinterpret_cast<long> (dlsym(sharedLibraryHandle, "JNI_CreateJavaVM")));
    if (createJavaVM == 0) {
      std::ostringstream os;
      os << "dlsym(\"" << sharedLibraryFilename << "\", JNI_CreateJavaVM) failed with " << dlerror() << ".";
      throw UsageError(os.str());
    }
    return createJavaVM;
  }
  
  jclass findClass(const std::string& className) {
    jclass javaClass = env->FindClass(className.c_str());
    if (javaClass == 0) {
      checkException();
      // Print out the CLASSPATH
      jclass systemClass = env->FindClass("java/lang/System");
      
      jmethodID systemId = env->GetStaticMethodID(systemClass,
              "getProperty",
              "(Ljava/lang/String;)Ljava/lang/String;");

      checkException();

      jstring propertyName = env->NewStringUTF("java.class.path");

      checkException();

      jstring property = (jstring) env->CallStaticObjectMethod(systemClass, systemId, propertyName);

      checkException();
      const char * propertyString = env->GetStringUTFChars(property, NULL);
      std::ostringstream os;
      os << "FindClass(\"" << className << "\") failed." << std::endl << "Try using / separated paths instead of . separated, for example: foo/bar/bif. Classpath was " << propertyString << "If classpath is empty, be sure to set java.class.path property by invoking with -Djava.class.path=\"$CLASSPATH\"";
      
      throw UsageError(os.str());
    }
    return javaClass;
  }
  
  jmethodID findMainMethod(jclass mainClass) {
    jmethodID method = env->GetStaticMethodID(mainClass, "main", "([Ljava/lang/String;)V");
    if (method == 0) {
      checkException();
      throw UsageError("GetStaticMethodID(\"main\") failed.");
    }
    return method;
  }
  
  jstring makeJavaString(const char* nativeString) {
    jstring javaString = env->NewStringUTF(nativeString);
    if (javaString == 0) {
      std::ostringstream os;
      os << "NewStringUTF(\"" << nativeString << "\") failed.";
      throw UsageError(os.str());
    }
    return javaString;
  }
  
  jobjectArray convertArguments(const NativeArguments& nativeArguments) {
    jclass jstringClass = findClass("java/lang/String");
    jstring defaultArgument = makeJavaString("");
    jobjectArray javaArguments = env->NewObjectArray(nativeArguments.size(), jstringClass, defaultArgument);
    if (javaArguments == 0) {
      std::ostringstream os;
      os << "NewObjectArray(" << nativeArguments.size() << ") failed.";
      throw UsageError(os.str());
    }
    for (size_t index = 0; index != nativeArguments.size(); ++ index) {
      std::string nativeArgument = nativeArguments[index];
      jstring javaArgument = makeJavaString(nativeArgument.c_str());
      env->SetObjectArrayElement(javaArguments, index, javaArgument);
    }
    return javaArguments;
  }
  
public:
  JavaInvocation(const std::string& jvmLibraryFilename, const NativeArguments& jvmArguments) {
    CreateJavaVM createJavaVM = findCreateJavaVM(jvmLibraryFilename.c_str());
    
    typedef std::vector<JavaVMOption> JavaVMOptions; // Required to be contiguous.
    JavaVMOptions javaVMOptions(jvmArguments.size());
    for (size_t ii = 0; ii != jvmArguments.size(); ++ ii) {
      // I'm sure the JVM doesn't actually write to its options.
      javaVMOptions[ii].optionString = const_cast<char*>(jvmArguments[ii].c_str());
    }
    
    JavaVMInitArgs javaVMInitArgs;
    javaVMInitArgs.version = JNI_VERSION_1_2;
    javaVMInitArgs.options = &javaVMOptions[0];
    javaVMInitArgs.nOptions = javaVMOptions.size();
    javaVMInitArgs.ignoreUnrecognized = false;
    
    int result = createJavaVM(&vm, reinterpret_cast<void**>(&env), &javaVMInitArgs);
    if (result < 0) {
      std::ostringstream os;
      os << "JNI_CreateJavaVM(" << javaVMOptions.size() << " options) failed with " << result << ".";
      throw UsageError(os.str());
    }
  }
  
  ~JavaInvocation() {
    // If you attempt to destroy the VM with a pending JNI exception,
    // the VM crashes with an "internal error" and good luck to you finding
    // any reference to it on google.
    if (env->ExceptionOccurred()) {
      env->ExceptionDescribe();
    }
    
    // The non-obvious thing about DestroyJavaVM is that you have to call this
    // in order to wait for all the Java threads to quit - even if you don't
    // care about "leaking" the VM.
    // Deliberately ignore the error code, as the documentation says we must.
    vm->DestroyJavaVM();
  }
  
  void invokeMain(const std::string& className, const NativeArguments& nativeArguments) {
    jclass javaClass = findClass(className);
    jmethodID javaMethod = findMainMethod(javaClass);
    jobjectArray javaArguments = convertArguments(nativeArguments);
    env->CallStaticVoidMethod(javaClass, javaMethod, javaArguments);
  }
};

struct LauncherArgumentParser {
private:
  JvmLocation jvmLocation;
  NativeArguments jvmArguments;
  std::string className;
  NativeArguments mainArguments;
  
private:
  static bool beginsWith(const std::string& st, const std::string& prefix) {
    return st.substr(0, prefix.size()) == prefix;
  }
  
public:
  LauncherArgumentParser(const NativeArguments& launcherArguments) {
    NativeArguments::const_iterator it = launcherArguments.begin();
    NativeArguments::const_iterator end = launcherArguments.end();
    while (it != end && beginsWith(*it, "-")) {
      std::string option = *it;
      if (option == "-client") {
        jvmLocation.setClientClass();
      } else if (option == "-server") {
        jvmLocation.setServerClass();
      } else {
        jvmArguments.push_back(option);
      }
      ++ it;
    }
    if (it == end) {
      throw UsageError("No class specified.");
    }
    className = *it;
    ++ it;
    while (it != end) {
      mainArguments.push_back(*it);
      ++ it;
    }
  }
  
  std::string getJvmLibraryFilename() const {
    return jvmLocation.findJvmLibraryFilename();
  }
  NativeArguments getJvmArguments() const {
    return jvmArguments;
  }
  std::string getClassName() const {
    return className;
  }
  NativeArguments getMainArguments() const {
    return mainArguments;
  }
};

int main(int, char** argv) {
  synchronizeWindowsEnvironment();
  programName = *argv;
  ++ argv;
  NativeArguments launcherArguments;
  while (*argv != 0) {
    launcherArguments.push_back(*argv);
    ++ argv;
  }
  try {
    LauncherArgumentParser parser(launcherArguments);
    JavaInvocation javaInvocation(parser.getJvmLibraryFilename(), parser.getJvmArguments());
    javaInvocation.invokeMain(parser.getClassName(), parser.getMainArguments());
  } catch (const UsageError& usageError) {
    std::ostringstream os;
    os << "Error: " << usageError.what() << std::endl;
    os << std::endl;
    os << "Usage: " << programName << " [options] class [args...]" << std::endl;
    os << "where options are:" << std::endl;
    os << "  -client - use client VM" << std::endl;
    os << "  -server - use server VM" << std::endl;
    os << "  -D<name>=<value> - set a system property" << std::endl;
    os << "     For example:"  << std::endl;
    os << "  -Djava.class.path=\"$CLASSPATH\" - set the classpath" << std::endl;

    os << "  -verbose[:class|gc|jni] - enable verbose output" << std::endl;
    // FIXME: If we know which version of JVM we've selected here, we could say so.
    os << "or JVM 1.5 or newer -X options." << std::endl;
    os << "class should be a / separated classname: Foo/Bar/Baz" << std::endl;
    os << std::endl;
    os << "Command line was:";
    os << std::endl;
    os << programName << " ";
    os << join(" ", launcherArguments);
    os << std::endl;
    std::cerr << os.str();
#ifdef __CYGWIN__
    std::string message = "Please copy this message to the clipboard with Ctrl-C and mail it to software@jessies.org.";
    message += "\n";
    message += "(Windows won't let you select the text but Ctrl-C works anyway.)";
    message += "\n";
    message += "\n";
    message += os.str();
    MessageBox(GetActiveWindow(), message.c_str(), "Launcher", MB_OK);
#endif
    return 1;
  }
}
