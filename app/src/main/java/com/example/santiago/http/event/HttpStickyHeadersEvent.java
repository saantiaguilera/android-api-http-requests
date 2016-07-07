package com.example.santiago.http.event;

import android.support.annotation.NonNull;

import com.example.santiago.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by santi on 06/07/16.
 */
public class HttpStickyHeadersEvent extends Event {

    private @NonNull List<String> removed;
    private @NonNull Map<String, String> added;

    public HttpStickyHeadersEvent() {
        removed = new ArrayList<>();
        added = new HashMap<>();
    }

    public HttpStickyHeadersEvent(@NonNull List<String> removed, @NonNull Map<String, String> added) {
        this.removed = removed;
        this.added = added;
    }

    //This is confusing...
    public void remove(String key) {
        removed.add(key);
    }

    public void add(String key, String value) {
        added.put(key, value);
    }

    public @NonNull List<String> getRemovedKeys() { return removed; }
    public @NonNull Map<String, String> getEntries() { return added; }

}
