function mode = getmode(position, theta, current_mode)
%GETMODE

[left, right] = getcenters(position, theta);
outsideleft = outside(left);
outsideright = outside(right);

switch current_mode
case 0
    if (outsideleft & outsideright)
        mode = 0;
    elseif (~outsideleft & outsideright)
        mode = 1;
    elseif (outsideleft & ~outsideright)
        mode = 2;
    else %if (~outsideleft & ~outsideright)
        %Check to see which circle has pentrated further.  Use the bias from
        %the further circle.  If both are equal bias rightward.
        dleft = distance(left);
        dright = distance(right);
        if dleft <= dright
            mode = 3;
        else %if dright < dleft
            mode = 4
        end
    end
case 1
    if (outsideleft & outsideright)
        mode = 0;
    elseif (~outsideleft & outsideright)
        mode = 1;
    elseif (outsideleft & ~outsideright)
        mode = 2;
    else %if (~outsideleft & ~outsideright)
        mode = 3;
    end
case 2
    if (outsideleft & outsideright)
        mode = 0;
    elseif (~outsideleft & outsideright)
        mode = 1;
    elseif (outsideleft & ~outsideright)
        mode = 2;
    else %if (~outsideleft & ~outsideright)
        mode = 4;
    end
case 3
    if (outsideleft & outsideright)
        mode = 0;
    elseif (~outsideleft & outsideright)
        mode = 1;
    else %if (~outsideright)
        mode = 3;
    end
case 4
    if (outsideleft & outsideright)
        mode = 0;
    elseif (outsideleft & ~outsideright)
        mode = 2;
    else %if (~outsideleft)
        mode = 4;
    end
otherwise
    error('Error--invalid mode input given to getbias.m');
end
