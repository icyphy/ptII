#!/usr/bin/python
import rospy
import socket
import fcntl
import struct
import re
import requests
#import httplib
import urllib
#import json
from geometry_msgs.msg import Twist
from std_msgs.msg import UInt8

#keyValueServer = "192.168.0.101:8077"
keyValueServer = "terra.eecs.berkeley.edu:8099"
aprilTagID = "20"

cmd_vel_pub = rospy.Publisher('cmd_vel', Twist, queue_size=1)
rospy.init_node('finite_pub_sub')

#duration (s) for an individual timedMovement component of an action
tick = 0.1

#multiplier for the speed of a timedMovement 
scale = 0.1

#number of timedMovement ticks to include in a qualitative movement
moveTicks = 20

#arc angular velocity
arcAngle = 0.333330

stop = Twist()

#x is linear velocity, z is angular velocity, time is seconds to move before stopping
def timedMovement( x, z, time ):
    go = Twist()
    go.linear.x = x
    go.angular.z = z
    cmd_vel_pub.publish(go)
    rospy.sleep(time)


def forward():
    #accelerate forward
    for i in range(0, moveTicks):
        timedMovement(i * scale, 0, tick)
        
    #accelerate backward
    for i in range(moveTicks -2, -moveTicks, -1):
        timedMovement(i * scale, 0, tick)
        
    #accelerate forward
    for i in range(-moveTicks + 2, 1):
        timedMovement( i * scale, 0, tick)

    #stop
    cmd_vel_pub.publish(stop)

def spin():
    #accelerate forward
    for i in range(0, moveTicks):
        timedMovement(0, i * scale, tick)
        
    #accelerate backward
    for i in range(moveTicks -2, -moveTicks, -1):
        timedMovement(0, i * scale, tick)
        
    #accelerate forward
    for i in range(-moveTicks + 2, 1):
        timedMovement(0, i * scale, tick)
    
    #stop
    cmd_vel_pub.publish(stop)

def arc():
    #accelerate forward
    for i in range(0, moveTicks):
        timedMovement(i * scale, arcAngle, tick)
        
    #accelerate backward
    for i in range(moveTicks -2, -moveTicks, -1):
        timedMovement(i * scale, arcAngle, tick)
        
    #accelerate forward
    for i in range(-moveTicks + 2, 1):
        timedMovement( i * scale, arcAngle, tick)

    #stop
    cmd_vel_pub.publish(stop)

def patrol():
    for j in range(0, 4):
        print
        #linear accelerate forward
        for i in range(0, moveTicks):
            timedMovement(i * scale, 0, tick)
            
        #linear accelerate backward to stop
        for i in range(moveTicks -2, -1, -1):
            timedMovement(i * scale, 0, tick)
        
        #angular accelerate forward
        for i in range(0, moveTicks):
            timedMovement(0, i * scale, tick)
        
        #angular accelerate backward to stop
        for i in range(moveTicks -2, -1, -1):
            timedMovement(0, i * scale, tick)
            
    #stop
    cmd_vel_pub.publish(stop)

def callback(msg):
    if msg.data == 1:
        print ("Forward")
        forward()
    elif msg.data == 2:
        print ("Spin")
        spin()
    elif msg.data == 3:
        print ("Patrol")
        patrol()
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
#Replaces all occurances. Must replace input for the RosSubscriber and the RosPublisher

params = urllib.urlencode({'id': aprilTagID})
#params = json.dumps({'id':  aprilTagID , 'value' : accessor })
print params
url = "http://" + keyValueServer + "/keyvalue/set?" + params
#url = "http://" + keyValueServer + "/keyvalue/set"
print url
#r = requests.get(url)
#r = requests.post(url, params)
r = requests.post(url, accessor)
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



    
