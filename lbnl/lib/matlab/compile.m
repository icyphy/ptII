%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This file compiles the BSD socket interface for Simulink.
% It is called by the makefile.
%
% On Windows, it requires the Microsoft compiler
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Note: We set the number of outputs to 1024, since matlab requires the
%       size of output arrays to be fixed.
%       If more elements are required, 
%        - change the entry y4[1024] in the assignment of 'funSpe' below, 
%        - change NDBLMAX=1024 in the file exchangeDoublesWithSocket.m,
%          and 
%        - change the entry #define NDBLMAX 1024 in the file lib/defines.h
%
%       If you change the number of I/O, you may want to regenerate the
%       Simulink blocks to update the graphic annotations with the
%       correct number of I/O values. (Regenerating the blocks does not
%       seem necessairy for the computation.)
%       Regenerating the blocks can be done by changing the Makefile.
%
%
%
% Compile matlab library
% This creates a file bcvtb.m with the function prototypes.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
os=deblank(getenv('BCVTB_OS'));
if (strcmp(os,'windows')) % have Windows  
  LIBBCVTB='..\util\bcvtb';
elseif (strcmp(os,'mac'))
  LIBBCVTB='../util/libbcvtb.dylib';
else
  LIBBCVTB='../util/libbcvtb.so';
end
incPat = [pwd, '/..'];
loadlibrary(LIBBCVTB, ...
            'matlabSocket.h', ...
            'includepath', './..', ...
            'mfilename', 'bcvtb.m')

% -------------------------------------------------------------------
% Check if Simulink is installed on this system. If Simulink is
% is not installed, exit. Otherwise, continue and compile 
% simulink libraries.
retVal=which('simulink');
if strcmp(retVal, '')
  disp('Simulink is not installed on this computer.')
  disp('The Simulink library will not be compiled.')
else

  makeSBlock=false;
  modelName = 'BSDSocket';

  def=legacy_code('initialize');
  def.SourceFiles={'simulinkSocket.c'};
  def.HeaderFiles={'simulinkSocket.h'};
  def.SampleTime  = 'inherited';
  
  funNam = {'establishBSDSocket', 'exchangeDoublesWithBSDSocket', 'closeBSDSocket'};
  funSpe = {'int16 y1 = establishBSDSocket(int16 u1[1])', ...
            'int16 y1 = exchangeDoublesWithBSDSocket(int16 u1[1], int16 u2[1], int16 y2[1], int16 u3[1], double u4[1], double u5[], double y3[1], double y4[1024])', ...
            'int16 y1 = closeBSDSocket(int16 u1[1], int16 u2[1])'};
  
  
  for i=1:3
    def.SFunctionName = char(funNam(i));
    def.OutputFcnSpec = char(funSpe(i));
    disp('### Generating mex file')
    legacy_code('sfcn_cmex_generate', def)
    disp('### Compiling code')
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%%%%%%%%%% fix from Mathworks (email from 2/7/08)
    fschange('');
    %% FSCHANGE is a Mathworks internal function that is used
    %% for troubleshooting purposes. It takes the name of a directory 
    %% as an input and forces the MATLAB path manager to 
    %% recheck the contents of the directory to check for 
    %% new (or deleted) files. When an empty string is 
    %% passed as an input the entire path is rechecked. 
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    try
      % Compilation for Windows
      if (strcmp(os, 'windows')) % have Windows    
        disp('### Compiling MATLAB interface for Windows')
        legacy_code('compile', def, {'../util/bcvtb.lib', 'simulinkSocket.c', '-I../util', '-I..'})
      else
        % Compilation for Mac OS X and Linux
        disp('### Compiling MATLAB interface for Mac OS X or Linux')
        
        legacy_code('compile', def, {'-lxml2', '-I..', '-L../util', '-lbcvtb'})
      end
    catch ME
      disp('*** Error when compiling MATLAB interface ###')
      disp(getReport(ME, 'extended'))
      disp('### Exit with error.')
      exit(1)
    end

    disp('### Returned from legacy_code')
    if makeSBlock
      disp('### Generating Simulink block')
      try
        legacy_code('slblock_generate', def, modelName)
      catch ME
        disp('*** Error when generating Simulink interface ***')
        disp(getReport(ME, 'extended'))
        disp('### Exit with error.')
        exit(1)
      end
    end
    % delete the files that we no longer need
    fn = [char(funNam(i)), '.c'];
    delete(fn)
  end
end
exit;
