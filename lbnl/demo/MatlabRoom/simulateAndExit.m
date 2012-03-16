%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This file is a simple simulation program written 
% in MATLAB to illustrate how to implement a client.
%
% The program simulates two rooms, each represented
% by a first order ordinary differential equation
% that describes the time rate of change of the
% room temperature.
% Input to the room model are the control signal
% for the heaters. The control signals are obtained from
% Ptolemy II. Output of the model are the room 
% temperatures, which are sent to Ptolemy II.
% The differential equation is solved using an 
% explicit Euler integration.
%
% MWetter@lbl.gov                                      2009-06-26
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Initialize model variables
delTim    = 60;  % time step
TIni      = 10;
tau       = 2*3600;
Q0Hea     = 100;
UA        = Q0Hea / 20;
TOut      = 5;
C         = [tau*UA 2*tau*UA];
TRoo      = [TIni TIni];
u         = [0 0];
% Initialize flags
retVal    = 0;
flaWri    = 0;
flaRea    = 0;
simTimWri = 0;
simTimRea = 0;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Add path to BCVTB matlab libraries
addpath( strcat(getenv('BCVTB_HOME'), '/lib/matlab'));
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Establish the socket connection
sockfd = establishClientSocket('socket.cfg');
if sockfd < 0
  fprintf('Error: Failed to obtain socket file descriptor. sockfd=%d.\n', ...
          sockfd);
  exit;
end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Loop for simulation time steps.
simulate=true;
while (simulate)
  % Assign values to be exchanged.
  try
    [retVal, flaRea, simTimRea, u ] = ...
        exchangeDoublesWithSocket(sockfd, flaWri, length(u), simTimWri, ...
                                  TRoo);
  catch ME1
    % exchangeDoublesWithSocket had an error. Terminate the connection
    processError(ME1, sockfd, -1);
    simulate=false;
  end
  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%   
  % Check return flags
  if (flaRea == 1) % End of simulation
    disp('Matlab received end of simulation flag from BCVTB. Exit simulation.');
    closeIPC(sockfd);
    simulate=false;
  end

  if (retVal < 0) % Error during data exchange
    exception = MException('BCVTB:RuntimeError', ...
                           'exchangeDoublesWithSocket returned value %d', ...
                           retVal);
    processError(exception, sockfd, -1);
    simulate=false;
  end

  if (flaRea > 1) % BCVTB requests termination due to an error.
    exception = MException('BCVTB:RuntimeError', ...
                           ['BCVTB requested MATLAB to terminate by sending %d\n', ...
                            'Exit simulation.\n'], retVal);
    processError(exception, sockfd, -1);
    simulate=false;
  end
  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%   
  % No flags have been found that require termination of the 
  % simulation. 
  % Having obtained u_k, we compute the new state x_k+1 = f(u_k)
  % This is the actual simulation of the client.
  if (simulate)
    for i=1:2
      TRoo(i) = TRoo(i) + ...
                delTim / C(i) * ( UA * (TOut-TRoo(i) ) + Q0Hea * u(i) );
    end
    % Advance simulation time
    simTimWri = simTimWri + delTim;
  end
end
exit
