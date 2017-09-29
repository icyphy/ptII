#!/usr/bin/python
import rospy
import socket
import fcntl
import struct
import re
import requests
#import httplib
import urllib
from geometry_msgs.msg import Twist
from std_msgs.msg import UInt8

#keyValueServer = "192.168.0.101/keyvalue/set:8077"
keyValueServer = "192.168.0.101:8077"
#keyValueServer = "terra.eecs.berkeley.edu:8090"

cmd_vel_pub = rospy.Publisher('cmd_vel', Twist, queue_size=1)
rospy.init_node('finite_pub_sub')



#x is linear velocity, z is angular velocity, time is seconds to move before stopping
def timedMovement( x, z, time ):
	stop = Twist()
	go = Twist()
	go.linear.x = x
	go.angular.z = z
	cmd_vel_pub.publish(go)
	rospy.sleep(time)
	cmd_vel_pub.publish(stop)

def callback(msg):
	if msg.data == 1:
		print ("Forward")
		timedMovement(1.0, 0, 3)
	elif msg.data == 2:
		print ("Spinning")
		timedMovement(0, 1.0, 3)
	else:
		print("Received Noop")


sub = rospy.Subscriber('finite', UInt8, callback)


#load accessor from file
with open('/home/lutz/catkin_ws/src/turtlebot3/turtlebot3_finite/src/RosCommand.js', 'r') as accessorFile:
	accessor=accessorFile.read()

#Get this machine's ip address and substitute it into the accessor
#Solution from http://code.activestate.com/recipes/439094-get-the-ip-address-associated-with-a-network-inter/
def getIpAddress(ifname):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    return socket.inet_ntoa(fcntl.ioctl(
        s.fileno(),
        0x8915,  # SIOCGIFADDR
        struct.pack('256s', ifname[:15])
    )[20:24])

ipAddress = getIpAddress('wlp1s0')
accessor = re.sub("localhost",ipAddress, accessor)

params = urllib.urlencode({'id': "18" , 'value' : accessor } )
url = "http://" + keyValueServer + "/keyvalue/set?" + params
print url
r = requests.get(url)
print r

#post accessor to key value store
#connection = httplib.HTTPConnection(keyValueServer)

#headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "*/*"}
#connection.request("POST", "/keyvalue/set", params, headers)
#connection = httplib.HTTPSConnection(keyValueServer)
#connection.request("GET", "/keyvalue/set?id=18&value=" + accessor)
#print (connection.getresponse().status)
#connection.close()
#postStatus = requests.post(keyValueServer, data="id=RosFinite.js&value=accessor")

rospy.spin(); #infinite loop prevents the program from terminating



	
