package com.usachevsergey.AssetBundleServer.security.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.thumbnails-dir}")
    private String thumbnailsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path thumbnailsPath = Paths.get(thumbnailsDir).toAbsolutePath();

        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:" + thumbnailsPath + "/");
    }
}
