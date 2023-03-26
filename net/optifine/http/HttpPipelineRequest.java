package net.optifine.http;

public class HttpPipelineRequest {
    private final HttpRequest httpRequest;
    private final HttpListener httpListener;
    private boolean closed;

    public HttpPipelineRequest(HttpRequest httpRequest, HttpListener httpListener) {
        this.httpRequest = httpRequest;
        this.httpListener = httpListener;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public HttpListener getHttpListener() {
        return httpListener;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
