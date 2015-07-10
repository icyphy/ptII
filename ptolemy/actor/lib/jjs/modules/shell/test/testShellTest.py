#!/usr/bin/python

import sys, getopt
import os
import signal
from threading import Thread
from threading import Event
from time import sleep


def reader(stopev) :
    while not stopev.is_set():
        line=sys.stdin.readline()
        if line: 
        	print line.upper()
		sys.stdout.flush()
	else :
		stopev.set()


def sporadic(stopev,d,n) :
    i=0
    while not stopev.is_set() :
        if i<n :
            print 'hello'
            sys.stdout.flush()
            i = i + 1
        sleep(d)


def runthreads(d,n) :
    t1_stop = Event()
    thread1 = Thread(target = reader, args = [t1_stop])
    thread1.start()
    thread2 = Thread(target = sporadic, args = [t1_stop,float(d),int(n)])
    thread2.start()
    #sleep(2)
    #t1_stop.set()
    thread1.join()
    thread2.join()

    print "Finished."
    sys.stdout.flush()


def main(argv) :
    dd=0
    nn=0

    try:
        opts, args = getopt.getopt(argv,"hd:n:")
    except getopt.GetoptError:
        print 'testShellTest.py -d <interval for sporadic msg> -n <numer of sporadic msg>'
        sys.exit(2)

    if not opts :
        print 'testShellTest.py -d <interval for sporadic msg> -n <numer of sporadic msg>'
        sys.exit(1)
        
    
    for opt, arg in opts:
        if opt == '-h':
            print 'testShellTest.py -d <interval for sporadic msg> -n <numer of sporadic msg>'
            sys.exit()
        elif opt in ("-d"):
            dd = arg
        elif opt in ("-n"):
            nn = arg

      
    runthreads(dd,nn)
    exit(0)



if __name__ == "__main__":
   main(sys.argv[1:])
