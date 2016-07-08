package com.example.santiago.http.event;

import com.example.santiago.event.Event;

/**
 * Connection timeouts in SECONDS
 * Created by saantiaguilera on 06/07/16.
 */
public class HttpTimeoutsEvent extends Event {

    private long conn, read, write;

    public HttpTimeoutsEvent(long conn, long read, long write) {
        this.conn = conn;
        this.read = read;
        this.write = write;
    }

    public long getConnectionTimeout() { return conn; }
    public long getReadTimeout() { return read; }
    public long getWriteTimeout() { return write; }

}
