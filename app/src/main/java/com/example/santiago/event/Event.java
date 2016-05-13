package com.example.santiago.event;

/**
 * Created by santiaguilera@theamalgama.com on 01/03/16.
 */
public abstract class Event {

    private int parentHash = -1;

    public void setParentHashCode(int parentHash) {
        this.parentHash = parentHash;
    }

    public int getParentHashCode() {
        if (parentHash != -1)
            return parentHash;

        return super.hashCode();
    }

}
