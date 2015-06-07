#!/usr/bin/python

import sys
import os
import signal
from threading import Thread
from threading import Event
from time import sleep



def reader(stopev) :
    while not stopev.is_set():
        line=sys.stdin.readline()
        if not line: break
        print line.upper()
        sys.stdout.flush()



t1_stop = Event()
thread1 = Thread(target = reader, args = [t1_stop])
thread1.start()
#sleep(2)
#t1_stop.set()
thread1.join()

print "Finished."
sys.stdout.flush()
exit(0)

