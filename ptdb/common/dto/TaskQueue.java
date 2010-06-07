package ptdb.common.dto;

import java.util.ArrayList;

public class TaskQueue extends ArrayList<Task> {

    private boolean m_executionCompleted = false;

    private boolean m_isProcessingError = false;

    private boolean m_isExecutionError = false;

    private String m_errorMessage;

    private boolean m_allTasksAdded = false;

    public void setAllTasksAdded() {
        m_allTasksAdded = true;
    }

    public boolean areAllTasksAdded() {
        return m_allTasksAdded;
    }

    public void setExecutionCompleted() {
        m_executionCompleted = true;
    }

    public boolean hasExecutionCompleted() {
        return m_executionCompleted;
    }

    public void setProcessingError() {
        m_isProcessingError = true;
    }

    public boolean hasProcessingError() {
        return m_isProcessingError;
    }

    public void setExecutionError(String errorMessage) {
        m_isExecutionError = true;
        this.m_errorMessage = errorMessage;
    }

    public boolean hasExecutionError() {
        return m_isExecutionError;
    }

    public String getExecutionErrorMessage() {
        return m_errorMessage;
    }

}
