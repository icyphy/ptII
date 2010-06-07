package ptdb.common.dto;

/**
 * This is an abstract class that defines an abstract task that can be executed over the database
 * 
 * @author wini
 */
public abstract class Task {
   
    /**
     * There are two types of tasks - Update/Write tasks that change the data in the database and Select/Read that read the data from the database
     * If this is true, then the task is an Update/Write task
     * If this is false, then the task is a Read/Select task
     */
    boolean _isUpdateTask;

    /**
     * Returns true if the given task is an update task or false if it is a selesct task
     * @return
     */
    public boolean isUpdateTask() {
        return _isUpdateTask;
    }
    
    /**
     * Sets the given task as an update task or select task depending on the value of isUpdateTask 
     * @param isUpdateTask - Boolean that specifies if the given task is an update task
     */
    public void setIsUpdateTask(boolean isUpdateTask) {
        _isUpdateTask = isUpdateTask;
    }
}
