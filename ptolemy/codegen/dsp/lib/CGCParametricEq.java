/* ParametricEq, CGC domain: CGCParametricEq.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCParametricEq.pl by ptlang
 */
/*
  Copyright (c) 1990-1997 The Regents of the University of California.
  All rights reserved.
  See the file $PTOLEMY/copyright for copyright notice,
  limitation of liability, and disclaimer of warranty provisions.
*/
package ptolemy.codegen.lib;

import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.codegen.kernel.ClassicCGCActor;
import ptolemy.codegen.kernel.ClassicPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCParametricEq
/**
   A two-pole, two-zero parametric digital IIR filter (a biquad).
   <p>
   The user supplies the parameters such as Bandwidth, Center Frequency,
   and Gain.  The digital biquad coefficients are quickly calculated
   based on the procedure defined by Shpak.

   @Author William Chen and John Reekie
   @Version $Id$, based on version 1.15 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCParametricEq.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCParametricEq extends CGCBiquad {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCParametricEq(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // choices are LOWpass=0,BANDpass=1,and HIpass=2 StringState
        filtertype = new Parameter(this, "filtertype");
        filtertype.setExpression("BAND");

        // given in Hz FloatState
        sampleFreq = new Parameter(this, "sampleFreq");
        sampleFreq.setExpression("44100");

        // Center frequency in Hz for bandpass and cutoff frequency for\nlow/highpass filters FloatState
        centerFreq = new Parameter(this, "centerFreq");
        centerFreq.setExpression("1000");

        // Passband frequency in Hz for low/highpass filters.\nNot needed for bandpass types. FloatState
        passFreq = new Parameter(this, "passFreq");
        passFreq.setExpression("1000");

        // given in dB and ranges from [-10,10]. FloatState
        gain = new Parameter(this, "gain");
        gain.setExpression("0");

        // Given in Octave and ranges from [0,4]. Not needed for Lowpass \nand Hipass types. FloatState
        bandwidth = new Parameter(this, "bandwidth");
        bandwidth.setExpression("1");

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     *  choices are LOWpass=0,BANDpass=1,and HIpass=2 parameter with initial value "BAND".
     */
    public Parameter filtertype;

    /**
     *  given in Hz parameter with initial value "44100".
     */
    public Parameter sampleFreq;

    /**
     *  Center frequency in Hz for bandpass and cutoff frequency for
     low/highpass filters parameter with initial value "1000".
    */
    public Parameter centerFreq;

    /**
     *  Passband frequency in Hz for low/highpass filters.
     Not needed for bandpass types. parameter with initial value "1000".
    */
    public Parameter passFreq;

    /**
     *  given in dB and ranges from [-10,10]. parameter with initial value "0".
     */
    public Parameter gain;

    /**
     *  Given in Octave and ranges from [0,4]. Not needed for Lowpass
     and Hipass types. parameter with initial value "1".
    */
    public Parameter bandwidth;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generatePreinitializeCode() {
        //# line 296 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCParametricEq.pl"
        addInclude("<string.h>");
        addGlobal(globalDecl, "global");
        addGlobal(declarations);
        CGCBiquad::initCode();
        addProcedure(setparams, "CGCParametricEq_setparams");
        addProcedure(constbw, "CGCParametricEq_constbw");
        addProcedure(lowpass, "CGCParametricEq_lowpass");
        addProcedure(hipass, "CGCParametricEq_hipass");
        addProcedure(bandpass, "CGCParametricEq_bandpass");
        addProcedure(setfiltertaps, "CGCParametricEq_setfiltertaps");
        addProcedure(selectFilter, "CGCParametricEq_selectFilter");
        addCode("$sharedSymbol(CGCParametricEq,setparams)(&$starSymbol(parametric),$ref(sampleFreq), $ref(passFreq), $ref(centerFreq), $ref(bandwidth), $ref(gain));");
        if (strcasecmp(filtertype, "LOW") == 0) {
	    addCode("$sharedSymbol(CGCParametricEq,lowpass)(&$starSymbol(parametric),$starSymbol(filtercoeff));");
        }
        else if (strcasecmp(filtertype, "HI") == 0) {
	    addCode("$sharedSymbol(CGCParametricEq,hipass)(&$starSymbol(parametric),$starSymbol(filtercoeff));");
        }
        else if (strcasecmp(filtertype, "BAND") == 0) {
	    addCode("$sharedSymbol(CGCParametricEq,bandpass)(&$starSymbol(parametric),$starSymbol(filtercoeff));");
        }
        addCode("$sharedSymbol(CGCParametricEq,setfiltertaps)(&$starSymbol(parametric),$starSymbol(filtercoeff),$starSymbol(filtertaps));");
    }

    /**
     */
    public void  generateFireCode() {
        //# line 320 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCParametricEq.pl"
        CGCBiquad::go();
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String setparams =
    "	  static void $sharedSymbol(CGCParametricEq,setparams)\n"
    + "	    (parametric_t *parametric, double sampleFreq, double\n"
    + "	     passFreq, double centerFreq, double bandwidth, double gain) {\n"
    + "	    double gaintmp, t0, invf1prime;\n"
    + "\n"
    + "	    parametric->T = 1/sampleFreq;\n"
    + "	    parametric->omegap = 2*PI*passFreq*parametric->T;\n"
    + "	    parametric->omegac = 2*PI*centerFreq*parametric->T;\n"
    + "	    t0 = log(2)/2;\n"
    + "	    invf1prime = exp(bandwidth*t0);\n"
    + "	    parametric->omegabw = parametric->omegac*(invf1prime-1/invf1prime);\n"
    + "\n"
    + "	    if (gain>=0){\n"
    + "	      parametric->gainflag = 1;\n"
    + "	      gaintmp=gain/20.0;\n"
    + "	    }\n"
    + "	    else {\n"
    + "	      parametric->gainflag = 0;\n"
    + "	      gaintmp=gain/-20.0;\n"
    + "	    }\n"
    + "	    parametric->lineargain = pow(10.0,gaintmp);\n"
    + "	  }\n";

    public String constbw =
    "	  /* Newton approximation */\n"
    + "          static double $sharedSymbol(CGCParametricEq,constbw)(int niter,\n"
    + "	     double tol, double bw, double wc, double initial_guess) {\n"
    + "	      double x0,x1;\n"
    + "	      double fval,fprimeval;\n"
    + "	      int i;\n"
    + "\n"
    + "	      x0 = initial_guess;\n"
    + "	      fval = -bw/2+atan(x0*wc/2)-atan(wc/(2*x0));\n"
    + "	      fprimeval = (wc/2)/(1+(x0*wc/2)*(x0*wc/2)) +\n"
    + "		(wc/2)/(x0*x0 + wc*wc/4);\n"
    + "	      x1 = x0 -	fval/fprimeval;\n"
    + " \n"
    + "	      i = 0;\n"
    + "	      while ((fabs(x1-x0) > tol) & (i < niter)){\n"
    + "		x0 = x1;\n"
    + "		fval = -bw/2+atan(x0*wc/2)-atan(wc/(2*x0));\n"
    + "		fprimeval = (wc/2)/(1+(x0*wc/2)*(x0*wc/2)) +\n"
    + "		  (wc/2)/(x0*x0 + wc*wc/4);\n"
    + "		x1 = x0 - fval/fprimeval;\n"
    + "		i++;\n"
    + "	      }\n"
    + "	      return x1;\n"
    + "            }        \n";

    public String lowpass =
    "          static void $sharedSymbol(CGCParametricEq,lowpass)(parametric_t *parametric, double *filtercoeff)\n"
    + "	    {\n"
    + "	      double omegapwarp,omegacwarp;\n"
    + "	      double n0,d0,d1,d2,Qzsquared,Qpsquared,Qz,Qp;\n"
    + "	      double a2,b2;\n"
    + "\n"
    + "	      omegapwarp = 2*tan(parametric->omegap/2);\n"
    + "	      omegacwarp = 2*tan(parametric->omegac/2);\n"
    + "	      n0  = ((omegapwarp*omegacwarp)*(omegapwarp*omegacwarp));\n"
    + "	      d0  = (parametric->lineargain)*pow(omegacwarp,4.0);\n"
    + "	      d1  = (parametric->lineargain-1) * ((omegacwarp*omegapwarp) *\n"
    + "				(omegapwarp*omegacwarp));\n"
    + "	      d2  = (parametric->lineargain*parametric->lineargain) *\n"
    + "		pow(omegapwarp,4.0);\n"
    + "	      Qzsquared = (n0)/(d0+d1-d2);\n"
    + "	      Qz = sqrt(Qzsquared);\n"
    + "	      Qpsquared =\n"
    + "		(parametric->lineargain*parametric->lineargain) /\n"
    + "		((parametric->lineargain-1)*(parametric->lineargain-1) +\n"
    + "		 1/(Qz*Qz));\n"
    + "	      Qp = sqrt(Qpsquared);\n"
    + "	      a2 = Qp*(Qz*(4+parametric->lineargain *\n"
    + "			   (omegapwarp*omegapwarp)) + 2*omegapwarp);\n"
    + "	      filtercoeff[0] =\n"
    + "		(2*Qp*Qz/a2)*(-4+parametric->lineargain *\n"
    + "			      (omegapwarp*omegapwarp));\n"
    + "	      filtercoeff[1] = (Qp/a2) *\n"
    + "		(Qz*(4+parametric->lineargain*(omegapwarp*omegapwarp))\n"
    + "		 - 2*omegapwarp);\n"
    + "	      b2 = Qz*(Qp*(4+(omegapwarp*omegapwarp))+2*omegapwarp);\n"
    + "	      filtercoeff[2] = (2*Qz*Qp/b2)*(-4+(omegapwarp*omegapwarp));\n"
    + "	      filtercoeff[3] = (Qz/b2) *\n"
    + "		(Qp*(4+(omegapwarp*omegapwarp))-2*omegapwarp);\n"
    + "	      filtercoeff[4] = a2/b2;\n"
    + "	    }\n";

    public String bandpass =
    "          static void $sharedSymbol(CGCParametricEq,bandpass)(parametric_t *parametric, double *filtercoeff) {\n"
    + "	      double omegacwarp,omegacornerwarp,omegacorner_guess,gamma;\n"
    + "	      double Qz,Qp,initial;\n"
    + "	      double a2,b2;\n"
    + "\n"
    + "	      omegacwarp = 2*tan(parametric->omegac/2);\n"
    + "	      omegacorner_guess = parametric->omegac - (parametric->omegabw/2);\n"
    + "	      initial = parametric->omegac/omegacorner_guess;\n"
    + "	      gamma = $sharedSymbol(CGCParametricEq,constbw)(5,0.001, parametric->omegabw, omegacwarp, initial);\n"
    + "	      omegacornerwarp = omegacwarp/gamma;\n"
    + "	      Qp = (sqrt(parametric->lineargain)*omegacwarp*omegacornerwarp) /\n"
    + "		(omegacwarp*omegacwarp-omegacornerwarp*omegacornerwarp);\n"
    + "	      Qz = Qp/parametric->lineargain;\n"
    + "\n"
    + "	      a2 = Qp*(Qz*(4+(omegacwarp*omegacwarp))+2*omegacwarp);\n"
    + "	      filtercoeff[0] = (2*Qp*Qz/a2)*(-4+(omegacwarp*omegacwarp));\n"
    + "	      filtercoeff[1] =\n"
    + "		(Qp/a2)*(Qz*(4+(omegacwarp*omegacwarp))-2*omegacwarp);\n"
    + "\n"
    + "	      b2 = Qz*(Qp*(4+(omegacwarp*omegacwarp))+2*omegacwarp);\n"
    + "	      filtercoeff[2] = (2*Qz*Qp/b2)*(-4+(omegacwarp*omegacwarp));\n"
    + "	      filtercoeff[3] =\n"
    + "		(Qz/b2)*(Qp*(4+(omegacwarp*omegacwarp))-2*omegacwarp);\n"
    + "	      filtercoeff[4] = a2/b2;\n"
    + "	    }\n";

    public String hipass =
    "          static void $sharedSymbol(CGCParametricEq,hipass)(parametric_t *parametric, double *filtercoeff)\n"
    + "	    {\n"
    + "	      double omegapwarp,omegacwarp;\n"
    + "	      double n0,d0,d1,d2,Qzsquared,Qpsquared,Qz,Qp;\n"
    + "	      double a2,b2;\n"
    + "\n"
    + "	      omegapwarp = 2*tan(parametric->omegap/2);\n"
    + "	      omegacwarp = 2*tan(parametric->omegac/2);\n"
    + "	      n0  = ((omegapwarp*omegacwarp)*(omegapwarp*omegacwarp));\n"
    + "	      d0  =\n"
    + "		(parametric->lineargain*parametric->lineargain) *\n"
    + "		pow(omegacwarp,4.0);\n"
    + "	      d1  = (parametric->lineargain-1) * ((omegacwarp*omegapwarp) *\n"
    + "				(omegapwarp*omegacwarp));\n"
    + "	      d2  = (parametric->lineargain)*pow(omegapwarp,4.0);\n"
    + "	      Qzsquared = (n0)/(-d0+d1+d2);\n"
    + "	      Qz = sqrt(Qzsquared);\n"
    + "	      Qpsquared =\n"
    + "		(parametric->lineargain*parametric->lineargain) /\n"
    + "		((parametric->lineargain-1) * (parametric->lineargain-1)\n"
    + "		 + 1/(Qz*Qz));\n"
    + "	      Qp = sqrt(Qpsquared);\n"
    + "	      a2 =\n"
    + "		Qp*(Qz * (4*parametric->lineargain +\n"
    + "			  (omegapwarp*omegapwarp)) + 2*omegapwarp);\n"
    + "	      filtercoeff[0] = (2*Qp*Qz/a2) *\n"
    + "		(-4*parametric->lineargain + (omegapwarp*omegapwarp));\n"
    + "	      filtercoeff[1] = (Qp/a2)\n"
    + "		*(Qz*(4*parametric->lineargain+(omegapwarp*omegapwarp))\n"
    + "		  - 2*omegapwarp);\n"
    + "	      b2 = Qz*(Qp*(4+(omegapwarp*omegapwarp))+2*omegapwarp);\n"
    + "	      filtercoeff[2] = (2*Qz*Qp/b2)*(-4+(omegapwarp*omegapwarp));\n"
    + "	      filtercoeff[3] = (Qz/b2) *\n"
    + "		(Qp*(4+(omegapwarp*omegapwarp))-2*omegapwarp);\n"
    + "	      filtercoeff[4] = a2/b2;\n"
    + "	    }\n";

    public String setfiltertaps =
    "	  static void $sharedSymbol(CGCParametricEq,setfiltertaps)\n"
    + "	    (parametric_t *parametric, double *filtercoeff, double\n"
    + "	     *filtertaps)\n"
    + "	    {\n"
    + "	    if (parametric->gainflag == 1){\n"
    + "	      filtertaps[0]=filtercoeff[2];\n"
    + "	      filtertaps[1]=filtercoeff[3];	      \n"
    + "	      filtertaps[2]=filtercoeff[4];\n"
    + "	      filtertaps[3]=filtercoeff[4]*filtercoeff[0];\n"
    + "	      filtertaps[4]=filtercoeff[4]*filtercoeff[1];\n"
    + "	    }\n"
    + "	    else {\n"
    + "	      filtertaps[0]=filtercoeff[0];\n"
    + "	      filtertaps[1]=filtercoeff[1];\n"
    + "	      filtertaps[2]=1/filtercoeff[4];\n"
    + "	      filtertaps[3]=filtercoeff[2]/filtercoeff[4];\n"
    + "	      filtertaps[4]=filtercoeff[3]/filtercoeff[4];\n"
    + "	    }\n"
    + "	  }\n";

    public String selectFilter =
    "	  /* This procedure is for the Tk star. Its a common	*/\n"
    + "	  /* procedure called to update changes in gain, center */\n"
    + "	  /* freq., bandwidth, and passband freq, in the Tk star*/\n"
    + "\n"
    + "	  static void $sharedSymbol(CGCParametricEq,selectFilter)\n"
    + "	    (parametric_t *parametric, double *filtercoeff, \n"
    + "	     double *filtertaps, char *filtername)\n"
    + "	    {\n"
    + "	      if (strcasecmp(filtername, \"LOW\") == 0){\n"
    + "		$sharedSymbol(CGCParametricEq,lowpass)\n"
    + "		  (parametric, filtercoeff);\n"
    + "	      }\n"
    + "	      else if (strcasecmp(filtername, \"HI\") == 0){\n"
    + "		$sharedSymbol(CGCParametricEq,hipass)\n"
    + "		  (parametric, filtercoeff);\n"
    + "	      }\n"
    + "	      else if (strcasecmp(filtername, \"BAND\") == 0){\n"
    + "		$sharedSymbol(CGCParametricEq,bandpass)\n"
    + "		  (parametric, filtercoeff);\n"
    + "	      }\n"
    + "	      $sharedSymbol(CGCParametricEq,setfiltertaps)\n"
    + "		(parametric, filtercoeff, filtertaps);\n"
    + "	    }\n";

    public String globalDecl =
    "	  typedef struct parametric_band {\n"
    + "	    double omegac;\n"
    + "	    double omegap;\n"
    + "	    double omegabw;\n"
    + "	    double T;\n"
    + "	    double lineargain;\n"
    + "	    int gainflag;\n"
    + "	  } parametric_t;\n"
    + "\n"
    + "#define PI (M_PI)\n";

    public String declarations =
    "	  parametric_t $starSymbol(parametric);\n"
    + "	  double $starSymbol(filtercoeff)[5]; \n";
}
