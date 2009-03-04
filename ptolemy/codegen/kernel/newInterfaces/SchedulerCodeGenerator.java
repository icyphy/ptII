package ptolemy.codegen.kernel.newInterfaces;

import ptolemy.codegen.util.PartialResult;

public interface SchedulerCodeGenerator{

    public PartialResult getSchedule();

    public PartialResult isValid();

    public PartialResult setValid(PartialResult PARAM);

}

