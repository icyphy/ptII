import com.izforge.izpack.panels.process.AbstractUIProcessHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Run miscellanous operations during setup.
 *  See https://izpack.atlassian.net/wiki/display/IZPACK/Executing+a+Java+Class+with+ProcessPanel
 */
public class IzpackSetup {
    /** Create a symbolic link for @accessors-hosts.
     *
     *  When we create a jar with a symbolic link, the contents of jar
     *  file will be the dereferenced symbolic link.  We first move
     *  the directory at the new link location, then try to create the
     *  link.  If we fail, we move the directory back.
     *
     *  @param args The list of arguments.  The first argument should
     *  be the installation directory.
     *  @return The message describing successor error.
     */
    public static String createLinks(String [] args) {
        // Create a symbolic link or, under Windows, a hard link.

        // This file returns strings instead of throwing exceptions
        // because that is how Izpack executes Java classes.

        if (args.length < 1) {
            return "IzpackSetup.java internal error: createLink() was invoked with no arguments?";
        }
                                
        Path installPath = Paths.get(args[0]);
        if (!Files.isDirectory(installPath)) {
            return installPath + " is not a directory?";
        }

        // Add a trailing File.separator
        String installDirectory = installPath.toAbsolutePath().toString();
        if (!installDirectory.endsWith(File.separator)) {
            installDirectory = installDirectory + File.separator;
        }

        StringBuffer results = new StringBuffer();

        // Create the Path in a platform-indendent manner.
        Path newLink = Paths.get(installDirectory + "org", "terraswarm", "accessor", "accessors", "web", "node_modules", "@accessors-hosts");
        Path temporary = Paths.get(installDirectory + "org", "terraswarm", "accessor", "accessors", "web", "node_modules", "@accessors-hosts.IzpackSetupTemp");
        Path target = Paths.get("..", "hosts");
        results.append(IzpackSetup.createLink(newLink, temporary, target));

        newLink = Paths.get(installDirectory + "org", "terraswarm", "accessor", "accessors", "web", "hosts", "browser", "common");
        temporary = Paths.get(installDirectory + "org", "terraswarm", "accessor", "accessors", "web", "hosts", "browser", "common.IzpackSetupTemp");
        target = Paths.get("..", "common");

        String commonMessage = IzpackSetup.createLink(newLink, temporary, target);
        if (commonMessage.length() > 0) {
            results.append("\n" + commonMessage);
        }

        // Darwin and Linux: Fix node/*/bin/npm
        String arch = null;
        String osName = System.getProperty("os.name");
        if (osName != null) {
            if (osName.startsWith("Darwin")) {
                arch = "darwin";
            } else if (osName.startsWith("Linux")) {
                arch = "linux";
            }
        }

        if (arch != null) {
            newLink = Paths.get(installDirectory, "vendors", "node", "node-v8.4.0-" + arch + "-x64", "bin", "npm");
            temporary = Paths.get(installDirectory + "vendors", "node", "node-v8.4.0-" + arch + "-x64", "bin","npm.tmp");
            target = Paths.get("..", "lib", "node_modules", "npm", "bin", "npm-cli.js");
            String npmMessage = IzpackSetup.createLink(newLink, temporary, target);
            if (npmMessage.length() > 0) {
                results.append("\n" + npmMessage);
            }
            Path npmCli = Paths.get(installDirectory + "vendors", "node", "node-v8.4.0-" + arch + "-x64", "lib", "node_modules", "npm", "bin", "npm-cli.js");
            npmCli.toFile().setExecutable(true);
        }

        return results.toString();
    }

    /** Create a link.
     *  @param newLink the link to be created
     *  @param temporary the path to the temporary location where the directory to be replaced by the link should be placed.
     *  @param target the target of the link to be created.
     *  @return Any messages about the creation of the link.
     */ 
    public static String createLink(Path newLink, Path temporary, Path target) {
        if (!Files.isReadable(newLink)) {
            Path currentRelativePath = Paths.get(".");
            return newLink + " does not exist?  That directory should be in the jar file so that we can move it aside.  The current relative path is " + currentRelativePath.toAbsolutePath();
        }

        try {
            // Save the directory that will be replaced by the link.
            Files.move(newLink, temporary);
        } catch (Throwable throwable) {
            return "Could not move " + newLink + " to " + temporary + ": " + throwable;
        }
        try {
            Files.createSymbolicLink(newLink, target);
        } catch (IOException ex) {
            String message = "Failed to create symbolic link or hard link from "
                + newLink + " to " + target + ": " + ex;
            try {
                Files.move(temporary, newLink);
            } catch (Throwable throwable) {
                message += " In addition, could not move " + temporary + " back to "
                    + newLink + ": " + throwable;
            }
            return message;
        } catch (UnsupportedOperationException ex2) {
            try {
                Files.createLink(newLink, target);
            } catch (Throwable ex3) {
                String message = "Failed to create symbolic link or hard link from "
                    + newLink + " to " + target + ": " + ex3;
                try {
                    Files.move(temporary, newLink);
                } catch (Throwable throwable) {
                    message += " In addition, could not move " + temporary
                        + " back to " + newLink + ": " + throwable;
                        }
                return message;
            }
        }
        String message = "Created link from " + newLink + " to " + target;
        try {
            IzpackSetup.deleteDirectory(temporary.toFile());
        } catch (Throwable throwable) {
            message += "  In addition, failed to delete " + temporary + ": "
                + throwable;
        }
        return message;
    }

    /** Delete a directory.
     * @param directory the File naming the directory.
     * @return true if the toplevel directory was deleted.
     */
    static public boolean deleteDirectory(File directory) {
        boolean deletedAllFiles = true;
	if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        if (!files[i].delete()) {
                            deletedAllFiles = false;
                        }
    	            }
                }
            }
	}
        return directory.delete() && deletedAllFiles;
    }

    /** For testing.
     *
     *  <p>To compile:</p>
     *  <pre>
     * javac -classpath $PTII/vendors/izpack-5.1.1/lib/izpack-api-5.1.1-SNAPSHOT.jar:$PTII/vendors/izpack-5.1.1/lib/izpack-panel-5.1.1-SNAPSHOT.jar IzpackSetup.java
     * </pre>
     *
     *  <p>To run:</p>
     *  <pre>
     * java -classpath .:$PTII/vendors/izpack-5.1.1/lib/izpack-api-5.1.1-SNAPSHOT.jar:$PTII/vendors/izpack-5.1.1/lib/izpack-panel-5.1.1-SNAPSHOT.jar IzpackSetup test/capeCodeTest
     * </pre>
     *
     * @param args The list of arguments.  The first argument should be the installation directory.
     *
     */
    public static void main(String[] args) {
        System.out.println(IzpackSetup.createLinks(args));
    }

    /** Izpack invokes this method.
     * @param handler The izpack handler.
     * @param args The list of arguments.  The first argument should be the installation directory.
     */
    public void run(AbstractUIProcessHandler handler, String[] args) {
        handler.logOutput(IzpackSetup.createLinks(args), false);
    }
}
