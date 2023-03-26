package net.optifine.http;

import net.minecraft.client.Minecraft;

public class FileDownloadThread extends Thread {
    private final String urlString;
    private final IFileDownloadListener listener;

    public FileDownloadThread(String urlString, IFileDownloadListener listener) {
        this.urlString = urlString;
        this.listener = listener;
    }

    public void run() {
        try {
            byte[] abyte = HttpPipeline.get(urlString, Minecraft.getMinecraft().getProxy());
            listener.fileDownloadFinished(urlString, abyte, null);
        } catch (Exception exception) {
            listener.fileDownloadFinished(urlString, null, exception);
        }
    }

    public String getUrlString() {
        return urlString;
    }

    public IFileDownloadListener getListener() {
        return listener;
    }
}
