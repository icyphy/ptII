#!/usr/bin/python

import sys
import os
import signal
from threading import Thread
from threading import Event
from time import sleep


def sporadic(runtime, stop_event):
    while(not stop_event.is_set()):
        print "goodbye"
        sys.stdout.flush()
        stop_event.wait(0.9)
        

def operate(stop_event):
    cnt = 0
    s = sys.stdin.readline().strip()
    while ((not stop_event.is_set()) and (cnt<4)) :
        sys.stdout.write(s.upper() + '\n')
        sys.stdout.flush()
        s = sys.stdin.readline().strip()
        cnt = cnt + 1

t1_stop = Event()
t2_stop = Event()

thread1 = Thread(target = sporadic, args = (5, t1_stop))
thread2 = Thread(target= operate, args=[t2_stop])

thread1.start()
thread2.start()

sleep(4)

t1_stop.set()
t2_stop.set()

thread1.join()
thread2.join()

print "Finished."
sys.stdout.flush()
exit(0)

