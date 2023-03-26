package net.optifine.http;

import java.util.Map;

public class FileUploadThread extends Thread {
    private final String urlString;
    private final Map headers;
    private final byte[] content;
    private final IFileUploadListener listener;

    public FileUploadThread(String urlString, Map headers, byte[] content, IFileUploadListener listener) {
        this.urlString = urlString;
        this.headers = headers;
        this.content = content;
        this.listener = listener;
    }

    public void run() {
        try {
            HttpUtils.post(urlString, headers, content);
            listener.fileUploadFinished(urlString, content, null);
        } catch (Exception exception) {
            listener.fileUploadFinished(urlString, content, exception);
        }
    }

    public String getUrlString() {
        return urlString;
    }

    public byte[] getContent() {
        return content;
    }

    public IFileUploadListener getListener() {
        return listener;
    }
}
