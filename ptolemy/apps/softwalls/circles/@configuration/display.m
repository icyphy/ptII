function display(c)
% CONFINGURATION/DISPLAY Display method for polynomial class.

newline = sprintf('\n');
str = sprintf(' =\n\n    X: %d\n    Y: %d\nTheta: %d', get(c, 'X'), ...
    get(c, 'Y'), get(c, 'Theta'));
disp([newline inputname(1) str newline])