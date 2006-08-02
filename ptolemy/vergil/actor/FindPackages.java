package ptolemy.vergil.actor;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.util.ExecuteCommands;



public class FindPackages {
    public static String findPackages(File ptII, ExecuteCommands executeCommands) {
        StringBuffer results = new StringBuffer();
        Set directoriesSeen = new HashSet();
        Set classFilesSeen = new HashSet();
        String ptIIPath = ptII.getPath().replace('\\', '/');
        if (executeCommands == null) {
            System.out.println("Searching for .class files in " + ptIIPath);
        } else {
            executeCommands.stdout("Searching for .class files in " + ptIIPath);
        }
        _getDirectories(ptII, directoriesSeen, classFilesSeen);
        Set packages = new HashSet();
        Iterator classFiles = classFilesSeen.iterator();
        while (classFiles.hasNext()) {
            File files[] = (File [])classFiles.next();
            for (int i = 0; i < files.length; i++) {
                String fullPath = files[i].toString().replace('\\', '/');
                String shortPath = fullPath.substring(0, files[i].toString().length() - 6);
                if (shortPath.startsWith(ptIIPath)) {
                    shortPath = shortPath.substring(ptIIPath.length() + 1);
                }
                shortPath = shortPath.substring(0, shortPath.lastIndexOf('/'));
                String packageName = shortPath.replace('/', '.');
                if (!packages.contains(packageName)) {
                    packages.add(packageName);
                    results.append( " " + packageName );           
                }
            }
        }
        if (executeCommands == null) {
            System.out.println(results.toString());
        } else {
            executeCommands.stdout(results.toString());
        }        
        return results.toString();
    }
     
    public static void main (String [] args) {
        findPackages(new File ("c:/cxh/pt II"), null); 
    }
    private static void _getDirectories(File directory, Set directoriesSeen, Set classFilesSeen) {
        File directories[] = directory.listFiles(new DirectoryFileFilter());
        for (int i = 0; i < directories.length; i++) {
            if (!directoriesSeen.contains(directories[i])
                    && !directories[i].getName().endsWith("adm")
                    && !directories[i].getName().endsWith("CVS")
                    && !directories[i].getName().endsWith("vendors")) {
                File classFiles[] = directories[i].listFiles(new ClassFileFilter());
                classFilesSeen.add(classFiles);
                directoriesSeen.add(directories[i]);
                _getDirectories(directories[i], directoriesSeen, classFilesSeen);
            }
        }
    }

    private static class ClassFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".class");
        }
    }
    
    private static class DirectoryFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }

}
