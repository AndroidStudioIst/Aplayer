package com.walixiwa.aplayer.model;

public class MyMediaInfo {
    private String uri;
    private long duration;

    public MyMediaInfo() {
    }

    public MyMediaInfo(String uri, long duration) {
        this.uri = uri;
        this.duration = duration;
    }

    public String getUri() {
        return uri;
    }

    public MyMediaInfo setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public MyMediaInfo setDuration(long duration) {
        this.duration = duration;
        return this;
    }
}
