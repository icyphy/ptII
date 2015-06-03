#!/usr/bin/python

import sys
from threading import Thread
from time import sleep

def sporadic(arg):
    for i in range(arg):
        print "running"
	sys.stdout.flush()
        sleep(1)

def operate():
	s = sys.stdin.readline().strip()
	while s not in ['break', 'quit']:
	    sys.stdout.write(s.upper() + '\n')
	    sys.stdout.flush()
	    s = sys.stdin.readline().strip()

thread1 = Thread(target = sporadic, args = (10, ))
thread2 = Thread(target= operate, args=[])
thread1.start()
thread2.start()
thread1.join()
thread2.join()

