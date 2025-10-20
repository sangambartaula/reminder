package com.sangambartaula.reminder;

import java.io.Serializable;

public class Reminder implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String taskName;
    private long triggerTime;
    private boolean recurring;
    private long intervalMillis;
    private boolean active;
    
    public Reminder(String taskName, long triggerTime, boolean recurring, long intervalMillis) {
        this.taskName = taskName;
        this.triggerTime = triggerTime;
        this.recurring = recurring;
        this.intervalMillis = intervalMillis;
        this.active = true;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public long getTriggerTime() {
        return triggerTime;
    }
    
    public boolean isRecurring() {
        return recurring;
    }
    
    public long getIntervalMillis() {
        return intervalMillis;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void setTriggerTime(long triggerTime) {
        this.triggerTime = triggerTime;
    }
    
    public boolean shouldTrigger(long currentTime) {
        return active && currentTime >= triggerTime;
    }
    
    public void reset(long currentTime) {
        this.triggerTime = currentTime + intervalMillis;
    }
}
