function [sys,x0,str,ts] =  planeplot(t,x,u,flag)
%PLANEPLOT Draw the aircraft on the screen, animation style.

switch flag,

  %%%%%%%%%%%%%%%%%%
  % Initialization %
  %%%%%%%%%%%%%%%%%%
  case 0,
    [sys,x0,str,ts]=mdlInitializeSizes;

  %%%%%%%%%%%%%%%
  % Derivatives %
  %%%%%%%%%%%%%%%
  case 1,
    sys=mdlDerivatives(t,x,u);

  %%%%%%%%%%
  % Update %
  %%%%%%%%%%
  case 2,
    sys=mdlUpdate(t,x,u);

  %%%%%%%%%%%
  % Outputs %
  %%%%%%%%%%%
  case 3,
    sys=mdlOutputs(t,x,u);

  %%%%%%%%%%%%%%%%%%%%%%%
  % GetTimeOfNextVarHit %
  %%%%%%%%%%%%%%%%%%%%%%%
  case 4,
    sys=mdlGetTimeOfNextVarHit(t,x,u);

  %%%%%%%%%%%%%
  % Terminate %
  %%%%%%%%%%%%%
  case 9,
    sys=mdlTerminate(t,x,u);

  %%%%%%%%%%%%%%%%%%%%
  % Unexpected flags %
  %%%%%%%%%%%%%%%%%%%%
  otherwise
    error(['Unhandled flag = ',num2str(flag)]);

end

% end sfuntmpl

%
%=============================================================================
% mdlInitializeSizes
% Return the sizes, initial conditions, and sample times for the S-function.
%=============================================================================
%
function [sys,x0,str,ts]=mdlInitializeSizes

%
% call simsizes for a sizes structure, fill it in and convert it to a
% sizes array.
%
% Note that in this example, the values are hard coded.  This is not a
% recommended practice as the characteristics of the block are typically
% defined by the S-function parameters.
%
sizes = simsizes;

sizes.NumContStates  = 2;
sizes.NumDiscStates  = 0;
sizes.NumOutputs     = 0;
sizes.NumInputs      = 2;
sizes.DirFeedthrough = 1;
sizes.NumSampleTimes = 1;   % at least one sample time is needed

sys = simsizes(sizes);

%
% x0 is the handle to the current figure + the handle to the aircraft on the
% screen.
%
figurehandle = createfigure;
centerhandle = drawaircraft(0, 0, figurehandle);

x0  = [figurehandle centerhandle];

%
% str is always an empty matrix
%
str = [];

%
% initialize the array of sample times
%
ts  = [0 0];

% end mdlInitializeSizes

%
%=============================================================================
% mdlDerivatives
% Return the derivatives for the continuous states.
%=============================================================================
%
%Don't change anything here, the state is just a handle.
function sys=mdlDerivatives(t,x,u)

xold = x;
figurehandle = x(1);
chandle = x(2);

chandle = drawaircraft(u(1), u(2), figurehandle, chandle);


sys = [0 0];     %This should always be [0 0]

% end mdlDerivatives

%
%=============================================================================
% mdlUpdate
% Handle discrete state updates, sample time hits, and major time step
% requirements.
%=============================================================================
%
function sys=mdlUpdate(t,x,u)

sys = [];

% end mdlUpdate

%
%=============================================================================
% mdlOutputs
% Return the block outputs.
%=============================================================================
%
function sys=mdlOutputs(t,x,u)

sys = [];

% end mdlOutputs

%
%=============================================================================
% mdlGetTimeOfNextVarHit
% Return the time of the next hit for this block.  Note that the result is
% absolute time.  Note that this function is only used when you specify a
% variable discrete-time sample time [-2 0] in the sample time array in
% mdlInitializeSizes.
%=============================================================================
%
function sys=mdlGetTimeOfNextVarHit(t,x,u)

sampleTime = 1;    %  Example, set the next hit to be one second later.
sys = t + sampleTime;

% end mdlGetTimeOfNextVarHit

%
%=============================================================================
% mdlTerminate
% Perform any end of simulation tasks.
%=============================================================================
%
function sys=mdlTerminate(t,x,u)

sys = [];

% end mdlTerminate

%
%=============================================================================
%drawaircraft
% Draws an aircraft.
%=============================================================================
%
function lh = drawaircraft(x,y,figurehandle,linehandle)
%HANDLE = DRAWAIRCRAFT(X,Y,FIGUREHANDLE)
%
%   Draws the aircraft at position (x,y) onto the figure specified by the
%   figure handle.  Returns the line handle representing the aircraft image.
%   
%HANDLE = DRAWAIRCRAFT(X,Y,FIGUREHANDLE,LINEHANDLE)
%
%   Erases the aircraft specified by the given linehandle before drawing the
%   other aircraft.

if nargin == 3
    figure(figurehandle)
    lh = plot(x,y, '.', 'MarkerSize', 24, 'EraseMode', 'Normal', ...
        'Color', [1 1 0]);
else
    lh = linehandle;
    if ~isequal(get(lh, 'EraseMode'), 'Normal')
        set(lh, 'EraseMode', 'Normal');
    end
    set(lh, 'XData', x, 'YData', y);
end

% end drawaircraft

%
%=============================================================================
%create figure
% Creates the background with no-fly zone.
%=============================================================================
%
function handle = createfigure
%HANDLE = CREATEFIGURE
%   
%   Returns a handle to a new figure, ready for drawing the aircraft onto.
%   The no-fly zone is included.

handle = figure;
hold on;
set(handle, 'DoubleBuffer', 'on');
axishandle = get(handle, 'CurrentAxes');
set(axishandle, 'Color', [0 0 0], 'XLim', [-0.2 4.2], 'YLim', [-2.2 2.2]);

x = [2 4 4 2];
y = [-1 -1 1 1];
fill(x,y,'r');

% end createfigure