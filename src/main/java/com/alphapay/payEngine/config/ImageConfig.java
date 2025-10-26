package com.alphapay.payEngine.config;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import javax.imageio.ImageIO;
import java.io.File;

@Configuration
class ImageIOConfig {
    @PostConstruct
    public void disableImageIOCache() {
        // Hard-disable disk cache (preferred for containers with read-only FS)
        ImageIO.setUseCache(false);
        // Also set a safe directory in case any library forces caching
        ImageIO.setCacheDirectory(new File("/tmp"));
        // Belt & suspenders: make sure the JVM tmp dir is /tmp
        System.setProperty("java.io.tmpdir", "/tmp");
    }
}