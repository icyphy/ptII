package lbnl.demo.SystemCommand;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;

/** Class that will be called by the SystemCommand actor using
  * a system call to the java virtual machine.
  * 
  * This class is used by the Ptolemy example file to demonstrate
  * how to call a system command using the SystemCommand actor. 
  * The class expects three arguments: The first argument is time, 
  * and the second and third argument are x1 and x2. 
  * All three arguments are written to the standard output
  * stream. In addition, x1 and x2 are written to the files
  * outputX1.txt and outputX2.txt
  */
class Simulate
{  
    public static void main(String args[])
    {
        // Make sure that we have three arguments
        if (args.length != 3) {
            System.err.println("Error: This program requires three arguments.");
            System.exit(1);
        }
        // Write arguments to standard output
        System.out.println("Time = " + args[0]);
        System.out.println("x1   = " + args[1]);
        System.out.println("x2   = " + args[2]);

        // Write arguments to files
        try{
            FileOutputStream fos = new FileOutputStream("outputX1.txt");
            new PrintStream(fos).println(args[1]);
            fos.close();
            fos = new FileOutputStream("outputX2.txt");
            new PrintStream(fos).println(args[2]);
            fos.close();
        }
        catch (IOException e){
            System.err.println ("Unable to write to file");
            System.exit(1);
        }
    }
}
