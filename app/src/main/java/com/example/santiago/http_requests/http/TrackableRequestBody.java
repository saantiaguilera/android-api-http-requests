package com.example.santiago.http_requests.http;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.Util;

import java.io.File;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class TrackableRequestBody extends RequestBody {
 
    private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE
 
    private final File file;
    private final ProgressListener listener;
    private final MediaType contentType;
    
    
    public TrackableRequestBody(File file, MediaType contentType, ProgressListener listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
    }
    
    @Override
    public long contentLength() {
        return file.length();
    }
    
    @Override
    public MediaType contentType() {
        return contentType;
    }
 
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
        	source = Okio.source(file);
            long total = 0;
            long read;
 
            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                
                if(listener!=null)
                	listener.transferred(total);
 
            }
        } finally {
            Util.closeQuietly(source);
        }
    }
    
    public interface ProgressListener {
        void transferred(long transferred);
    }
 
}