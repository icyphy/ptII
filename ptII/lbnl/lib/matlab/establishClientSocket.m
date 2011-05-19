function sockfd = establishClientSocket(socketConfigurationFile)
% ESTABLISHCLIENTSOCKET - Establishes the client connection to the socket
%
%   sockfd = establishClientSocket('socket.cfg') connect MATLAB to 
%   the BCVTB. This function will read the file socket.cfg to get
%   information for how to connect to the BCVTB.
%  
%   If successful, sockfd will be a positive integer for the 
%   socket file descriptor. In case of error, sockfd will be negative.
%  
%   This function will also load the library libbcvtb unless it is
%   already loaded.

%  Revision history
%  ----------------  
%  2009-06-26 MWetter@lbl.gov: First version, based on code of 
%             Charles Corbin, UC Boulder
  
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
  status = -1; 
else
  status = 1; 
end

% Establish the socket connection
if and(libisloaded(BCVTBLIB), (status == 1))
  sockfd = calllib(BCVTBLIB,'establishclientsocket', socketConfigurationFile); 
else
  disp('Could not load library establishClientSocket.m');
  sockfd = -1;
end
