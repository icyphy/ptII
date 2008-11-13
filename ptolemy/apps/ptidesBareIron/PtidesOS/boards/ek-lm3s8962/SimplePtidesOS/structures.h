/******************************************************************************/
/** Event definition for PtidyOS

 Copyright (c) 1997-2008 The Regents of the University of California.
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

 @author Jia Zou
 @Parts of this code that serves to interface the hardware is copied from
 previous written code samples by Slobodan Matic and Agilent

 */
/******************************************************************************/

typedef struct{
    double doubleValue;
} Value;

typedef struct
{
    long timestamp;
    int microstep;
} Tag;

typedef struct Actor Actor;
typedef struct Event Event;

struct Actor 
{

	//FIXME: should have a linked list of next_actors...
    struct Actor *nextActor1;
    struct Actor *nextActor2;
	unsigned int inputPortCount;
	unsigned int outputPortCount;
	unsigned int WCET;
	char type;

	void (*fireMethod)	(Actor*, Event*);							   

	unsigned int sourceActor;
	unsigned int multipleInputs;

	unsigned int firing;

	//does the actor also need to keep a local event queue?
    //actor methods
    //preinitialize();T_
    //initialize()
    //prefire() returns true to indicate that the actor is ready to fire
    //prefire();
    //fire();
    //postfire();
    //wrapup();
};

struct Event 
{
    Value thisValue;
    Tag Tag;
	char inUse;   // used b/c smallest size avaliable.. really stores -1, or index in eventMemory
	int name;
    Actor* actorFrom;
    //an event would only have 1 actorToFire
    Actor* actorToFire;

    struct Event* next;

};



