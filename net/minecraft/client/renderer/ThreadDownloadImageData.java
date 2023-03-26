package net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.http.HttpPipeline;
import net.optifine.http.HttpRequest;
import net.optifine.http.HttpResponse;
import net.optifine.player.CapeImageBuffer;
import net.optifine.shaders.ShadersTex;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadDownloadImageData extends SimpleTexture {
    private static final Logger logger = LogManager.getLogger();
    private static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);
    private final File cacheFile;
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    public Boolean imageFound;
    public boolean pipeline;
    private BufferedImage bufferedImage;
    private Thread imageThread;
    private boolean textureUploaded;

    public ThreadDownloadImageData(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, IImageBuffer imageBufferIn) {
        super(textureResourceLocation);
        cacheFile = cacheFileIn;
        imageUrl = imageUrlIn;
        imageBuffer = imageBufferIn;
    }

    private void checkTextureUploaded() {
        if (!textureUploaded && bufferedImage != null) {
            textureUploaded = true;

            if (textureLocation != null) {
                deleteGlTexture();
            }

            if (Config.isShaders()) {
                ShadersTex.loadSimpleTexture(super.getGlTextureId(), bufferedImage, false, false, Config.getResourceManager(), textureLocation, getMultiTexID());
            } else {
                TextureUtil.uploadTextureImage(super.getGlTextureId(), bufferedImage);
            }
        }
    }

    public int getGlTextureId() {
        checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage bufferedImageIn) {
        bufferedImage = bufferedImageIn;

        if (imageBuffer != null) {
            imageBuffer.skinAvailable();
        }

        imageFound = bufferedImage != null;
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        if (bufferedImage == null && textureLocation != null) {
            super.loadTexture(resourceManager);
        }

        if (imageThread == null) {
            if (cacheFile != null && cacheFile.isFile()) {
                logger.debug("Loading http texture from local cache ({})", new Object[]{cacheFile});

                try {
                    bufferedImage = ImageIO.read(cacheFile);

                    if (imageBuffer != null) {
                        setBufferedImage(imageBuffer.parseUserSkin(bufferedImage));
                    }

                    loadingFinished();
                } catch (IOException ioexception) {
                    logger.error("Couldn't load skin " + cacheFile, ioexception);
                    loadTextureFromServer();
                }
            } else {
                loadTextureFromServer();
            }
        }
    }

    protected void loadTextureFromServer() {
        imageThread = new Thread("Texture Downloader #" + threadDownloadCounter.incrementAndGet()) {
            public void run() {
                HttpURLConnection httpurlconnection = null;
                logger.debug("Downloading http texture from {} to {}", new Object[]{imageUrl, cacheFile});

                if (shouldPipeline()) {
                    loadPipelined();
                } else {
                    try {
                        httpurlconnection = (HttpURLConnection) (new URL(imageUrl)).openConnection(Minecraft.getMinecraft().getProxy());
                        httpurlconnection.setDoInput(true);
                        httpurlconnection.setDoOutput(false);
                        httpurlconnection.connect();

                        if (httpurlconnection.getResponseCode() / 100 != 2) {
                            if (httpurlconnection.getErrorStream() != null) {
                                Config.readAll(httpurlconnection.getErrorStream());
                            }

                            return;
                        }

                        BufferedImage bufferedimage;

                        if (cacheFile != null) {
                            FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), cacheFile);
                            bufferedimage = ImageIO.read(cacheFile);
                        } else {
                            bufferedimage = TextureUtil.readBufferedImage(httpurlconnection.getInputStream());
                        }

                        if (imageBuffer != null) {
                            bufferedimage = imageBuffer.parseUserSkin(bufferedimage);
                        }

                        setBufferedImage(bufferedimage);
                    } catch (Exception exception) {
                        logger.error("Couldn't download http texture: " + exception.getClass().getName() + ": " + exception.getMessage());
                    } finally {
                        if (httpurlconnection != null) {
                            httpurlconnection.disconnect();
                        }

                        loadingFinished();
                    }
                }
            }
        };
        imageThread.setDaemon(true);
        imageThread.start();
    }

    private boolean shouldPipeline() {
        if (!pipeline) {
            return false;
        } else {
            Proxy proxy = Minecraft.getMinecraft().getProxy();
            return (proxy.type() == Type.DIRECT || proxy.type() == Type.SOCKS) && imageUrl.startsWith("http://");
        }
    }

    private void loadPipelined() {
        try {
            HttpRequest httprequest = HttpPipeline.makeRequest(imageUrl, Minecraft.getMinecraft().getProxy());
            HttpResponse httpresponse = HttpPipeline.executeRequest(httprequest);

            if (httpresponse.getStatus() / 100 != 2) {
                return;
            }

            byte[] abyte = httpresponse.getBody();
            ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte);
            BufferedImage bufferedimage;

            if (cacheFile != null) {
                FileUtils.copyInputStreamToFile(bytearrayinputstream, cacheFile);
                bufferedimage = ImageIO.read(cacheFile);
            } else {
                bufferedimage = TextureUtil.readBufferedImage(bytearrayinputstream);
            }

            if (imageBuffer != null) {
                bufferedimage = imageBuffer.parseUserSkin(bufferedimage);
            }

            setBufferedImage(bufferedimage);
        } catch (Exception exception) {
            logger.error("Couldn't download http texture: " + exception.getClass().getName() + ": " + exception.getMessage());
        } finally {
            loadingFinished();
        }
    }

    private void loadingFinished() {
        imageFound = bufferedImage != null;

        if (imageBuffer instanceof CapeImageBuffer capeimagebuffer) {
            capeimagebuffer.cleanup();
        }
    }

    public IImageBuffer getImageBuffer() {
        return imageBuffer;
    }
}
