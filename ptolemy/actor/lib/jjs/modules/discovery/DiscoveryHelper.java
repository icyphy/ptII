/* A helper class for the device discovery accessor.

   Copyright (c) 2015-2016 The Regents of the University of California.
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

 */

package ptolemy.actor.lib.jjs.modules.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// DiscoveryHelper

/**
   A helper class for the device discovery Javascript host code.
   It handles execution of the ping and arp commands and returns device
   information to the accessor.

   @author Elizabeth Latronico, contributor: Christopher Brooks
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Red (ltrnc)
   @Pt.AcceptedRating Red (ltrnc)
 */
public class DiscoveryHelper {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a new DiscoveryHelper.
     */
    public DiscoveryHelper() {
        _ipMap = new HashMap<String, JSONObject>();
    }

    /** Discover all devices that reply to a ping on the class-C local
     *  area network connected to the given IP address
     *
     *  A class-C network has a netmask of /24, or 255.255.255.0.
     *
     *  Returns a string representation of a JSON array of devices.  Note that
     *  returning the JSONArray directly does not work properly - for some
     *  reason, the JSONArray of device objects is not converted into a
     *  Javascript array of device objects.
     *
     *  @param IPAddress The IP address whose subnet should be scanned.  E.g.,
     *  for IP address 192.168.5.7, scan 192.168.5.0 to 192.168.5.255.
     *  @param discoveryMethod The discovery method to be used, e.g. nmap.
     *  @return A String containing a JSON representation of devices found.
     */
    public String discoverDevices(String IPAddress, String discoveryMethod) {
        // FIXME: We probably want to take a broadcast address as an
        // input and ping that to get all the hosts.  Pinging 1
        // through 255 works for class C subnets.
        // Unfortunately, some devices do not respond to broadcast pings,
        // but may respond to a direct ping
        // https://reggle.wordpress.com/2011/09/14/broadcast-pings-do-they-work/
        if (_debugging) {
            System.out.println("DiscoveryHelper.discover(" + IPAddress + ")");
        }
        _ipMap.clear();
        _hostIP = IPAddress;

        String baseIP;

        if (IPAddress.lastIndexOf(".") > 0) {
            baseIP = IPAddress.substring(0, IPAddress.lastIndexOf("."));

            if (discoveryMethod.equalsIgnoreCase("nmap")) {
                if (_debugging) {
                    System.out.println("Discovery - using nmap");
                }
                _nmap(baseIP);
            } else {

                // Select appropriate function for the OS
                if (System.getProperty("os.name").substring(0, 3)
                        .equalsIgnoreCase("Win")) {

                    // Run pings concurrently, in separate processes
                    _processes = new ArrayList();

                    // FIXME: this only works on a Class C network,
                    // where the IP addresses are 0-255.
                    for (int i = 0; i <= 255; i++) {
                        try {
                            Process process = Runtime.getRuntime().exec(
                                _pingWindowsCommand + baseIP + "." + i);
                            _processes.add(process);
                        } catch (IOException e) {
                            System.err.println("Error executing ping for " +
                                    baseIP + "." + i);
                        }
                    }

                    // Read all data
                    for (int i = 0; i < _processes.size(); i++) {
                        _readPingWindows(_processes.get(i), baseIP + "." + i);
                    }

                    // Wait for all processes to finish
                    for (int i = 0; i < _processes.size(); i++) {
                        try {
                            _processes.get(i).waitFor();
                        } catch (InterruptedException e) {
                            // Don't wait for it if interrupted
                        }
                    }

                    _arpWindows();

                } else {
                    if (_debugging) {
                        System.out
                                .println("Discovery: Run pings concurrently, in separate threads. baseIP: "
                                        + baseIP);
                    }
                    // Run pings concurrently, in separate processes
                    _processes = new ArrayList();

                    for (int i = 0; i <= 255; i++) {
                        try {
                            Process process = Runtime.getRuntime().exec(
                                _pingLinuxCommand + baseIP + "." + i);
                            _processes.add(process);
                        } catch (IOException e) {
                            System.err.println("Error executing ping for " +
                                    baseIP + "." + i);
                        }
                    }

                    // Read all data.
                    for (int i = 0; i < _processes.size(); i++) {
                        _readPingLinux(_processes.get(i), baseIP + "." + i);
                    }

                    // Wait for all processes to finish.
                    for (int i = 0; i < _processes.size(); i++) {
                        try {
                            _processes.get(i).waitFor();
                        } catch (InterruptedException e) {
                            // Don't wait for it if interrupted.
                        }
                    }
                    _arpLinux();
                }
            }
        } else {
            System.err.println("DiscoveryHelper.discover(" + IPAddress
                    + "): \"" + IPAddress + "\" does not have a period?");

            // TODO:  Return error message?  What should accessors do in case
            // of error?
        }

        JSONArray jArray = new JSONArray();

        // Return a string representation of a JSON array of JSON objects
        if (_ipMap.size() > 0) {
            for (String key : _ipMap.keySet()) {
                jArray.put(_ipMap.get(key));
            }
            return jArray.toString();
        } else {
            System.err.println("DiscoveryHelper.discover(" + IPAddress + "): "
                    + "no devices found? Returning [].");
            return jArray.toString();
        }
    }

    /** Return the IP address of the host machine.
     *
     * @return The IP address of the host machine.
     */
    public /*static*/ String getHostAddress() {
        InetAddress address = _getUsefulAddress();
        if (address == null) {
            return null;
        }
        String hostAddress = address.getHostAddress();
        return hostAddress;
    }

    
    /** Get the MAC (Media Access Control) address
     *  of the first non-loopback, non-multicast address.
     *  @return the MAC address
     *  @exception SocketException If thrown while finding the address.
     */
    public /*static*/ String getMacAddress() throws SocketException {
        InetAddress address = _getUsefulAddress();
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
        byte [] macAddress = networkInterface.getHardwareAddress();

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < macAddress.length; i++) {
            buffer.append(String.format("%02X%s", macAddress[i], (i < macAddress.length - 1) ? "-" : ""));
        }
        return buffer.toString();
    }

    /** Execute the arp command on a Linux platform.  The arp command finds
     *  names and MAC addresses for devices on the local area network.
     *  The arp command should follow a ping sweep to get the most up-to-date
     *  information and to screen out devices that are not accessible at the
     *  moment (which may be cached in the arp cache).
     */
    private void _arpLinux() {
        if (_ipMap.size() == 0) {
            System.err
                    .println("Warning, no devices were found.  Perhaps the format returned by "
                            + _pingLinuxCommand + " is different?");
        }
        try {
            Process process = Runtime.getRuntime().exec(_arpCommand);

            BufferedReader stdOut = null;
            try {
                stdOut = new BufferedReader(new InputStreamReader(
                                                                  process.getInputStream(), "UTF-8"));
                String line;

                while ((line = stdOut.readLine()) != null) {
                    if (_debugging) {
                        System.out.println("Discovery: arp returns \"" + line
                                + "\"");
                    }
                    StringTokenizer tokenizer = new StringTokenizer(line, " ");
                    String token, name, ip;

                    // Example arp data:
                    // <incomplete> for not-found MACs
                    // EPSON3FDF60 (192.168.5.2) at ac:18:26:3f:bf:20 [ether] on
                    //  eth0
                    // NAUSPIT8 (192.168.5.9) at <incomplete> on eth0
                    // The host machine will not be listed

                    if (tokenizer.countTokens() >= 4) {
                        // Check if IP address has been added to _ipMap by ping
                        // If not, skip this device - it's unavailable

                        // Tokens are: name, IP address, "at", mac
                        name = (String) tokenizer.nextElement();
                        token = (String) tokenizer.nextElement();
                        ip = token.substring(1, token.length() - 1);

                        if (_debugging) {
                            System.out.println("Discovery: name: " + name
                                    + ", mac: " + token + " ,ip: " + ip);
                        }
                        JSONObject object;
                        for (String key : _ipMap.keySet()) {
                            object = _ipMap.get(key);
                            if (object.get("IPAddress").toString()
                                    .equalsIgnoreCase(ip)) {
                                token = (String) tokenizer.nextElement();
                                token = (String) tokenizer.nextElement();
                                object.put("name", name);
                                object.put("mac", token);

                                _ipMap.put(key, object);
                            }
                        }
                    }
                }
            } finally {
                if (stdOut != null) {
                    stdOut.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error executing " + _arpCommand);
        } catch (JSONException e2) {
            // If error, assume problem with MAC, since that's the only new info
            System.err
                    .println("Arp error: MAC address is not JSON compatible.");
        }
    }

    /** Execute the arp command on a Windows platform.  The arp command finds
     *  MAC addresses for devices on the local area network.
     *  The arp command should follow a ping sweep to get the most up-to-date
     *  information and to screen out devices that are not accessible at the
     *  moment (which may be cached in the arp cache).
     */
    private void _arpWindows() {
        if (_ipMap.size() == 0) {
            System.err
                    .println("Warning, no devices were found.  Perhaps the format returned by "
                            + _pingWindowsCommand + " is different?");
        }
        try {
            Process process = Runtime.getRuntime().exec(_arpCommand);

            BufferedReader stdOut = null;
            try {
                stdOut = new BufferedReader(new InputStreamReader(
                                                                  process.getInputStream(), "UTF-8"));
                String line;
                int index;
                JSONObject object;

                while ((line = stdOut.readLine()) != null) {
                    for (String key : _ipMap.keySet()) {
                        object = _ipMap.get(key);
                        index = line.indexOf(object.getString("IPAddress"));
                        if (index != -1) {
                            // The Interface: IP entry is the host machine.  Its mac
                            // is not listed.  Would need ipconfig /all to get it
                            // TODO:  Do we want host mac?
                            if (index != 2) {
                                object.put("mac", "Host machine");
                            } else {
                                // MAC address is fixed # of chars after IP address
                                object.put("mac",
                                        line.substring(index + 22, index + 39));
                            }
                            _ipMap.put(key, object);
                        }
                    }
                }
            } finally {
                if (stdOut != null) {
                    stdOut.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error executing " + _arpCommand);
        } catch (JSONException e2) {
            // If error, assume problem with MAC, since that's the only new info
            System.err
                    .println("Arp error: MAC address is not JSON compatible.");
        }
    }

    /** Return the first non-loopback, non-multicast Network Interface.
     *  @return the Network Interface.
     */
    private static InetAddress _getUsefulAddress() {
        // Based on:
        // http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
        // Ignore loopback (127.*) and broadcast (255.*)
        // Others could be private (site-local) (192.*, 10.*, 172.16.*
        // through 172.31.*), link local (169.254.*), or multicast
        // (224.* through 239.*)
        String hostAddress = "Unknown";
        InetAddress address;
        NetworkInterface networkInterface;

        try {
            // Coverity Scan: "getNetworkInterfaces returns null".
            Enumeration<NetworkInterface> interfaces =
                NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return null;
            }
            Enumeration<InetAddress> addresses;

            while (interfaces.hasMoreElements()) {
                networkInterface = interfaces.nextElement();
                addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();
                    hostAddress = address.getHostAddress();
                    // Break at first non-loopback, non-multicast address
                    if (!address.isLoopbackAddress() &&
                            !address.isMulticastAddress() &&
                            // Avoid addresses such as fe80:0:0:0:0:5efe:c0a8:3801%net3
                            // Assumes IPv4 address
                            !hostAddress.contains(":")) {
                        return address;
                    }
                }
            }
        } catch(SocketException e) {
            return null;
        }
        return null;
    }

    /** Execute the nmap command.  Nmap finds IP addresses, names and MAC
     *  addresses on the local area network.  The nmap program requires separate
     *  installation on Mac and Windows; please see: https://nmap.org/
     *
     *  @param baseIP The IP address of the host minus the subnet portion.
     */
    private void _nmap(String baseIP) {
        try {
            // FIXME: This assumes that .1 is the correct
            // network and /24 is the correct netmask.
            String command = _nmapCommand + baseIP + ".1/24";
            if (_debugging) {
                System.out.println("Discovery: about to execute " + command);
            }
            Process process = Runtime.getRuntime().exec(command);


            BufferedReader stdOut = null;

            try {
                stdOut = new BufferedReader(new InputStreamReader(
                                                                  process.getInputStream(), "UTF-8"));

                String line;

                // Sample nmap output:
                // Starting Nmap 6.47 ( http://nmap.org ) at 2015-05-02 18:08 Eastern Daylight Time
                //
                // Nmap scan report for EPSON3FBF20 (192.168.5.1)
                // Host is up (0.12s latency).
                // MAC Address: AD:19:27:3E:BE:21 (Seiko Epson)
                // Nmap scan report for NP-2L543W046400 (192.168.5.4)
                // Host is up (0.12s latency).
                // MAC Address: DD:3B:5F:D0:B7:B2 (Roku)
                // Nmap done: 256 IP addresses (5 hosts up) scanned in 13.59 seconds

                while ((line = stdOut.readLine()) != null) {
                    // Look for "Nmap scan"
                    if (line.startsWith("Nmap scan")) {
                        _readDeviceNmap(line, stdOut);
                    }
                }
            } finally {
                if (stdOut != null) {
                    stdOut.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error executing " + _nmapCommand);
        }
    }

    /** Read results of a ping on a Linux or Mac platform. If a device is found,
     * add a JSON object containing the device IP address and name to _ipMap.
     *
     * @param process  The process that is executing the ping.
     * @param testIP  The IP address to base the sweep on.
     */
    private void _readPingLinux(Process process, String testIP) {
        JSONObject device = null;

        try {
            BufferedReader stdOut = null;
            try {
                stdOut = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
                String line;

                while ((line = stdOut.readLine()) != null) {
                    if (_debugging) {
                        System.out.println("pingLinux(" + testIP + "): " + line);
                    }
                    // Example reply from a device that's on and available
                    // PING 192.168.5.6 (192.168.5.6) 56(84) bytes of data.
                    // 64 bytes from 192.168.5.6: icmp_seq=1 ttl=64 time=381 ms
                    // 64 bytes from 192.168.5.6: icmp_seq=2 ttl=64 time=97.9 ms
                    //
                    // --- 192.168.254.6 ping statistics ---
                    // 2 packets transmitted, 2 received, 0% packet loss, time 1000ms

                    // Look for "2 received" or "1 received" (Ok if one dropped)

                    int found = line.indexOf("2 received");
                    if (found < 0) {
                        found = line.indexOf("1 received");
                        if (found < 0) {
                            // Think different.  Mac OS returns something a bit different
                            // bash-3.2$ bash-3.2$ ping -c 2 192.168.1.2
                            // PING 192.168.1.2 (192.168.1.2): 56 data bytes
                            // 64 bytes from 192.168.1.2: icmp_seq=0 ttl=255 time=6.733 ms
                            // 64 bytes from 192.168.1.2: icmp_seq=1 ttl=255 time=5.336 ms
                            //
                            // --- 192.168.1.2 ping statistics ---
                            // 2 packets transmitted, 2 packets received, 0.0% packet loss
                            // round-trip min/avg/max/stddev = 5.336/6.034/6.733/0.699 ms
                            // bash-3.2$

                            found = line.indexOf("2 packets received");
                            if (found < 0) {
                                found = line.indexOf("1 packets received");
                            }
                        }
                    }

                    if (found > 0) {
                        // Store IP.  Name and mac address are determined in arp
                        // The host machine is pingable but has no arp entry
                        if (_debugging) {
                            System.out.println("Device available at " + testIP);
                        }
                        try {
                            if (testIP.equalsIgnoreCase(_hostIP)) {
                                device = new JSONObject("{\"IPAddress\": " + testIP
                                        + "," + "\"name\": \"Host machine\""
                                        + ", \"mac\": \"Host machine\"}");
                            } else {
                                device = new JSONObject("{\"IPAddress\": " + testIP
                                        + "," + "\"name\": \"Unknown\""
                                        + ", \"mac\": \"Unknown\"}");
                            }

                        } catch (JSONException e) {
                            System.err.println("Error creating JSON object "
                                    + "for device at IP " + testIP);
                        }
                    }
                }
            } finally {
                if (stdOut != null) {
                    stdOut.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error executing ping for " + testIP);
        }
        // Lock _ipMap?  No two devices will have same IP address, so no
        // collisions.
        if (device != null) {
            _ipMap.put(testIP, device);
        }
    }

    /** Read results of a ping on a Windows platform.  If a device is found,
     * add a JSON object containing the device IP address and name to _ipMap.
     *
     * @param process  The process that is executing the ping.
     * @param testIP  The IP address to base the sweep on.
     */
    private void _readPingWindows(Process process, String testIP) {
        JSONObject device = null;

        BufferedReader stdOut = null;
        try {
            stdOut = new BufferedReader(new InputStreamReader(
                                                              process.getInputStream(), "UTF-8"));

            StringBuffer data = new StringBuffer();
            String line;

            while ((line = stdOut.readLine()) != null) {
                data.append(line);
            }

            // Example reply from a device that's on and available:
            // Pinging EPSON3FDF60 [192.168.5.19] with 32 bytes of data:
            // Reply from 192.168.5.19: bytes=32 time=75ms TTL=64
            // Reply from 192.168.5.19: bytes=32 time=4ms TTL=64

            // If the device is on and available, the second line should
            // be of the form:  Reply from IPAddress: bytes=n
            // Look for "bytes=" (other responses may contain just "bytes")
            if (data.length() > 0) {
                int found = data.indexOf("bytes=");
                if (found != -1) {
                    // Get name of device
                    int bracket = data.indexOf("[");
                    String name = data.substring(8, bracket - 1);

                    if (_debugging) {
                        System.out.println("Device " + name + " available at "
                                + testIP);
                    }
                    try {
                        device = new JSONObject("{\"IPAddress\": " + testIP
                                + "," + "\"name\": " + name
                                + ", \"mac\": \"Unknown\"}");
                    } catch (JSONException e) {
                        // If error, assume problem with name
                        System.err.println("Device name " + name + " is not "
                                + "JSON compatible.");
                    }
                }
            }
            process.destroy();
        } catch (IOException ex) {
            System.err.println("Error executing ping for " + testIP + ": " + ex);
        } finally {
            if (stdOut != null) {
                try {
                    stdOut.close();
                } catch (IOException ex2) {
                    System.err.println("Error closing stdout for " + testIP + ": " + ex2);
                }
            }
        }

        // Lock _ipMap?  No two devices will have same IP address, so no
        // collisions.
        if (device != null) {
            _ipMap.put(testIP, device);
        }


    }

    /** Read device information from a stream of Nmap output and save to _ipMap.
     *
     * @param line The previous line read
     * @param stdOut The stream to read device information from.
     */
    private void _readDeviceNmap(String line, BufferedReader stdOut) {
        try {
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            String token, name = "Unknown", mac = "Unknown", ip;

            // First line has ip and sometimes name
            for (int i = 1; i <= 4; i++) {
                token = tokenizer.nextToken(); // Nmap scan report for
            }

            token = tokenizer.nextToken();
            // name is present, e.g. EPSON3FBF20 (192.168.5.1)
            if (tokenizer.hasMoreTokens()) {
                name = token;
                ip = tokenizer.nextToken();
                ip = ip.substring(1, ip.length() - 1);
            } else {
                // name not present, e.g. 192.168.5.1
                ip = token;
            }

            line = stdOut.readLine();

            // Second line should say "Host is up"
            if (line != null && line.startsWith("Host is up")) {

                // Third line, if present, has mac address.  Not always present.
                line = stdOut.readLine();
                if (line != null) {
                    if (line.startsWith("MAC")) {
                        tokenizer = new StringTokenizer(line, " ");
                        token = tokenizer.nextToken();
                        token = tokenizer.nextToken();
                        mac = tokenizer.nextToken();
                    } else if (line.startsWith("Nmap scan")) {
                        _readDeviceNmap(line, stdOut);
                    }
                }

                // Add to table if at least IP exists and host is up
                try {
                    if (_debugging) {
                        System.out.println("Discovery: " + "name: " + name
                                + ", mac: " + mac + " , ip: " + ip);
                    }

                    JSONObject device = new JSONObject("{ \"IPAddress\": " + ip
                            + ", " + "\"name\": " + name
                            + ", \"mac\": Unknown }");
                    // Have to put MAC separately due to colons in MAC
                    device.put("mac", mac);

                    _ipMap.put(ip, device);
                } catch (JSONException e) {
                    System.err.println("Nmap error: Can't "
                            + "create JSON object for device at " + "IP " + ip);
                }
            }
        } catch (IOException e) {
            System.err.println("Error executing " + _nmapCommand);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The command to invoke the arp, the address resolution protocol. */
    private String _arpCommand = "arp -a";

    /** Flag for debugging mode. */
    private final boolean _debugging = false;

    /** The IP address of the host machine.  Used to label it as such in list.*/
    private String _hostIP;

    /** A map storing IP address to JSON objects with device info. */
    private HashMap<String, JSONObject> _ipMap;

    /** The command to invoke nmap, a network scanner, followed by a trailing
     *  space.  The nmap command is the same for all OSes.
     *  Note that nmap requires a separate installation on Mac and Windows.
     *  Please see:  https://nmap.org/
     */
    private String _nmapCommand = "nmap -sn ";

    /** Command to ping an IP address under Linux, followed by a trailing space. */
    private String _pingLinuxCommand = "ping -c 2 ";

    /** Command to ping an IP address under Windows, followed by a trailing space. */
    private String _pingWindowsCommand = "ping -n 2 -a ";

    /** Set of processes started, so that we can wait for them to finish. */
    private ArrayList<Process> _processes;
}
