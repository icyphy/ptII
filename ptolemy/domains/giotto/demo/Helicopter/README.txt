I just met Prof. Sastry, he said, it's OK.
I'm attaching control.giotto file which was used in real flight test in January and slides which were made last november.

I commented each ports and tasks briefly in the control.giotto
Let me add some more comments.

1. 'firstpos' is the reference postion when mode is switched to 'actuate' mode. To prevent the helicopter from jumping in control gain, it needs the initial position when control is engaged.
2. 'servosDirect' in the estimate mode is used to read transmitter data and write the data to the servo without adding any control outputs. I inserted this because even though it is in 'estimate' mode, I wanted everything was in the loop to reduce chance of any jumping at the mode switching.

If you have any question, please, let me know.

Best,
Jongho

----- Original Message -----
From: Christopher Brooks <cxh@eecs.berkeley.edu>
Date: Wednesday, February 2, 2005 2:35 pm
Subject: Re: FW: [Escher] Giotto Code Gen

> Hi Jongho,
> 
> Thanks for meeting with me, sorry I was distracted with the calendar
> work.  I ended up getting it all to work, so now I can pay attention
> to Giotto.
> 
> Next Thursday, I'll be at Vanderbilt in Nashville, Tennessee, speaking
> to roughly 10 for the Escher project.
> 
> So this is not BEARS, this is something else.
> 
> Professor Sastry knows about Escher.
> 
> The work we are doing for Escher involves converting
> Simulink/Stateflow models to ECSL_DP and then to Ptolemy and then to
> Giotto.
> 
> What I would like from you is a copy of your slides, so I can use some
> of them (and credit you) and a copy of the Giotto code so I can see
> about creating a Simulink model.  I don't particularly need the
> functional code, just the Giotto code.
> 
> Thanks!
> 
> _Christopher
> 
> 
> --------
> 
>    Hi Christopher,
>    
>    Could you say again what kind of speaking you have on next 
> thursday? where?
>    for what? Did you mean BEARS conference?
>    
>    Then, I'll talk with prof. Sastry.
>    Thanks,
>    Jongho
>    
>    ----- Original Message -----
>    From: Christopher Brooks <cxh@eecs.berkeley.edu>
>    Date: Tuesday, February 1, 2005 4:22 pm
>    Subject: Re: FW: [Escher] Giotto Code Gen
>    
>    > Sorry, but I have to leave at 5.
>    > We could try meeting at 11am on Wednesday in 337.
>    > I think I'm also meeting with Jonathan Sprinkle at that time.
>    > 
>    > _Christopher
>    > --------
>    > 
>    >    Hi Christopher,
>    >    
>    >    What I'm doing is to implement time-triggering embedded 
>    > control software fo
>    >   r Berkeley UAV using Giotto.
>    >    I'll be happy if I can talk with you what I'm doing and 
> what 
>    > you're doing.
>    >    We can chat after Chess semiar. But If you are OK, can we 
> meet 
>    > in the morni
>    >   ng on Wednesday?
>    >    
>    >    Thanks,
>    >    Jongho
>    >    
>    >    
>    >    
>    >    ----- Original Message -----
>    >    From: Christopher Brooks <cxh@eecs.berkeley.edu>
>    >    Date: Monday, January 31, 2005 8:20 am
>    >    Subject: Re: FW: [Escher] Giotto Code Gen
>    >    
>    >    > Hi Jongho,
>    >    > 
>    >    > In the short term, I'm primarily interested in going from
>    >    > Simulink/Stateflow through ECSL-DP to Ptolemy II to 
> Giotto.  
>    > This is
>    >    > my goal for the February 10th Escher meeting.
>    >    > 
>    >    > Codegen to QNX seems pretty interesting though, thanks 
> for 
>    > the tip.
>    >    > 
>    >    > I was on vacation Thursday and Friday.  I'll be on 
> campus on 
>    > Tuesday,    > perhaps we could chat after the Chess Seminar? 
> If 
>    > you want to 
>    >    > have a
>    >    > more formal meeting with me, we can set up a time 
> without cc'ing
>    >    > Professor Shankar and the Escher list.
>    >    > 
>    >    > _Christopher
>    >    > 
>    >    > 
>    >    > Professor Sastry wrote:
>    >    > --------
>    >    > 
>    >    >    Dear Jongho,
>    >    >    
>    >    >    You need to try to meet with Christopher in normal 
>    > business hours.
>    >    >    How about tomorrow morning, if he is available.
>    >    >    Thanks,
>    >    >    Shankar
>    >    >    
>    >    >    -----Original Message-----
>    >    >    From: jongho@EECS.Berkeley.EDU 
> [jongho@EECS.Berkeley.EDU]    >    >    Sent: Wednesday, January 
> 26, 2005 4:38 PM
>    >    >    To: cxh@EECS.Berkeley.EDU
>    >    >    Cc: Shankar Sastry
>    >    >    Subject: Re: FW: [Escher] Giotto Code Gen
>    >    >    
>    >    >    
>    >    >    Hello Chrisopher,
>    >    >    
>    >    >    I have a Giotto compiler for QNX6 from Slobodan Matic.
>    >    >    If you need to talk with me, please let me know.
>    >    >    I'll be in 333 Cory from 8:00pm today and I'll also 
>    > available 
>    >    > after 4:00pm
>    >    >    tomorrow.
>    >    >    
>    >    >    Regards,
>    >    >    Jongho
>    >    >    
>    >    >    
>    >    >    ----- Original Message -----
>    >    >    From: Shankar Sastry <sastry@eecs.berkeley.edu>
>    >    >    Date: Wednesday, January 26, 2005 10:05 am
>    >    >    Subject: FW: [Escher] Giotto Code Gen
>    >    >    
>    >    >    > Jongho,
>    >    >    > Please talk to Christopher and work with him.
>    >    >    > Thanks,
>    >    >    > shanakr
>    >    >    > 
>    >    >    > -----Original Message-----
>    >    >    > From: escher-admin@chess.eecs.berkeley.edu
>    >    >    > [escher-admin@chess.eecs.berkeley.edu]On Behalf Of 
>    > Christopher    >    > Brooks
>    >    >    > Sent: Wednesday, January 26, 2005 8:40 AM
>    >    >    > To: eal@maury.eecs.berkeley.edu
>    >    >    > Cc: ptresearch@maury.eecs.berkeley.edu; 
>    >    > escher@chess.eecs.berkeley.edu    > Subject: [Escher] 
> Giotto 
>    > Code Gen
>    >    >    > 
>    >    >    > 
>    >    >    > Ok, I hacked on Giotto Code Gen so that now when 
> we 
>    > generate 
>    >    >    > Giotto code, we can create the Java stub files by 
> using    >    >    > a script called 
>    >    > $PTII/ptolemy/domains/giotto/kernel/mkGiottoCGJava    > 
>    >    >    > The Giotto file can then be loaded in to the 
> Giotto 
>    >    > Development Kit
>    >    >    > (GDK) tool and compiled.  Giotto E Code is created 
> and when
>    >    >    > run, the Java stub files are called.
>    >    >    > 
>    >    >    > I copied GiottoSimple.xml from the Ptolemy II 
> design 
>    > doc, 
>    >    > renamed    > it to Simple.xml and placed it in
>    >    >    > $PTII/ptolemy/domains/giotto/demo/Simple.  I 
> annotated 
>    > it with
>    >    >    > instructions on how to run the Giotto Code Generator.
>    >    >    > 
>    >    >    > I was able to create, compile and run Giotto code for
>    >    >    > the Giotto Multimode and Giotto Hierarchy demos.
>    >    >    > The Codegen does not work yet for the Giotto 
> Composite 
>    > demo 
>    >    > because    > the top level is not Giotto.
>    >    >    > 
>    >    >    > The next step would be to create Simulink models for
>    >    >    > $PTII/ptolemy/domains/giotto/demo/Simple/Simple.xml
>    >    >    > 
> $PTII/ptolemy/domains/giotto/demo/Hierarchy/Hierarchy.xml    >    
> >    > $PTII/ptolemy/domains/giotto/demo/Multimode/Multimode.xml
>    >    >    > 
>    >    >    > so that I can then run them through ECSL_DP and 
> create 
>    > the 
>    >    > Ptolemy    > models.
>    >    >    > 
>    >    >    > Maybe we can look at creating these Simulink 
> models at 
>    > 10am?    >    > 
>    >    >    > _Christopher
>    >    >    > _______________________________________________
>    >    >    > Escher mailing list
>    >    >    > Escher@chess.eecs.berkeley.edu
>    >    >    > http://chess.eecs.berkeley.edu/escher/listinfo/escher
>    >    >    > 
>    >    > --------
>    >    > 
>    > --------
>    > 
> --------
> 
