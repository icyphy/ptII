function theta = malicious_heading(position)
% MALICIOUS_HEADING   Calculates the malicious-pilot heading 
% 
% THETA = MALICIOUS_HEADING(POSITION) Returns the heading at the current
% position.

a1 = 2;
a2 = 4;
b1 = -1;
b2 = 1;

x = position(1);
y = position(2);

if y < b1
    if x < a1
        theta = atan2(b1 - y, a1 - x);
    elseif x <=a2   
        %         and x >= a1
        theta = pi / 2;
    else
        %         x > a2
        theta = atan2(b1 - y, a2 - x);
    end
elseif y <= b2
    %     and y >= b1
    if x < a1
        theta = 0;
    elseif x <= a2
        error('Pilot has entered the no-fly zone');
    else
        %         x > a2
        theta = - pi;
    end
else
    %     y > b1
    if x < a1
        theta = atan2(b2 - y, a1 - x);
    elseif x <= a2
        %         and x >= a1
        theta = - pi / 2;
    else
        %         x > a2
        theta = atan2(b2 - y, a2 - x);
    end
end
