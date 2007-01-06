import x10.*;
import x10.net.*;

public class X10SerialServer
{
    private static void usage()
    {
        System.out.println("Usage:  X10SerialServer [COMPORT] [CONTROLLER TYPE]");
        System.out.println("  COMPORT - COM1, COM2, /dev/ttyS0, /dev/ttyS1");
        System.out.println("  CONTROLLER TYPE - CM11A or CM17A");
        System.out.println("  If no CONTROLLER TYPE is specified, then CM11A is used by default");
        System.exit(1);
    }
    
    public static void main(String[] args) throws Exception
    {
        if(args.length < 1)
        {
            usage();
        }
        else
        {
            String comport = args[0];
            String controllerType = "CM11A";            
            if(args.length > 1)
            {
                controllerType = args[1];
            }
            
            Controller controller = null;
            if("CM17A".equalsIgnoreCase(controllerType))
            {
                controller = new CM17ASerialController(comport);
            }
            else if("CM11A".equalsIgnoreCase(controllerType))
            {
                controller = new CM11ASerialController(comport);
            }
            else
            {
                usage();
            }
            ControllerServer cs = new ControllerServer(controller, 2400);
            cs.start();
            System.out.println("Threads Started...");
            System.in.read();
        }
    }
}