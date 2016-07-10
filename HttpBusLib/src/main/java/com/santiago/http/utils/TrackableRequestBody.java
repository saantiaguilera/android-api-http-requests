package com.santiago.http.utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class TrackableRequestBody extends RequestBody {

    private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE

    private final File file;

    private final WeakReference<ProgressListener> listener;

    private final String contentType;

    public TrackableRequestBody(File file, String contentType, ProgressListener listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSource source = null;

        try {
            //TODO Test this, im not too sure its like this
            source = Okio.buffer(Okio.source(file));

            long total = 0;
            long read;

            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                listener.get().transferred(total);
            }
        } finally {
            Util.closeQuietly(source);
        }
    }

    public interface ProgressListener {
        void transferred(long num);
    }

}
