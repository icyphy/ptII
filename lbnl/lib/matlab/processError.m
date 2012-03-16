function retVal = processError(mException, sockfd, signal)
% PROCESSERROR - Processes a MATLAB exception
%
%   retVal = processError(mException, sockfd, signal) processes
%   a MATLAB exception. 
%   The function writes an error message to the console.
%   Next, if the BCVTB library can be loaded
%   and if sockfd is not a negative number, then 
%   this function sends 'signal' to the socket.
%   
%   If sending a message to the socket was successful, then
%   'retVal' will be zero.
%  
%   This function will also load the library libbcvtb unless it is
%   already loaded.

%  Revision history
%  ----------------  
%  2011-10-20 MWetter@lbl.gov: First version.
  
% Print error message to console
fprintf(['*** Error: ', getReport(mException)]);
fprintf(['           ', 'Trying to send client error from MATLAB to the BCVTB.']);
  
% Load library if it is not already loaded
BCVTBLIB=getBCVTBLibName();
if ~libisloaded(BCVTBLIB)
  loadlibrary(BCVTBLIB, @bcvtb);
end

if ~libisloaded(BCVTBLIB)    
  msg='Error. Failed to load BCVTB library.';
  ME = MException('BCVTB:ConnectError', ...
                  msg);
  throw(ME);
  retVal = -1; 
else
  retVal = sendClientError(sockfd, signal); 
end
