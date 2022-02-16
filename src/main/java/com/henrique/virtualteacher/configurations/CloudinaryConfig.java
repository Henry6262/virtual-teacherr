package com.henrique.virtualteacher.configurations;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Component
public class CloudinaryConfig {

   private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "henrique-mk",
            "api_key", "888986857524757",
            "api_secret", "ChKs_-9FO57ZsCP_OWmY9NFdtWk",
           "secure", true));

    public CloudinaryConfig () {
    }

    public String upload(MultipartFile file) throws IOException {
        File convertedFile = new File(System.getProperty("java.io.tmpdir")+"/"+file.getName());
        file.transferTo(convertedFile);
        var uploader = cloudinary.uploader();
        var result = uploader.upload(convertedFile, ObjectUtils.emptyMap());
        return result.get("url").toString();
    }

    public void destroy(String url) throws Exception {
        cloudinary.uploader().destroy(url, new HashMap<>());
    }


}
