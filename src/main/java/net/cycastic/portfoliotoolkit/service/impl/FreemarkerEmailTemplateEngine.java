package net.cycastic.portfoliotoolkit.service.impl;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.service.EmailImage;
import net.cycastic.portfoliotoolkit.service.EmailTemplateEngine;
import net.cycastic.portfoliotoolkit.service.StoragePresigner;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.text.StringEscapeUtils;

@Service
@RequiredArgsConstructor
public class FreemarkerEmailTemplateEngine implements EmailTemplateEngine {
    private final StoragePresigner storagePresigner;

    private static Configuration getBaseConfiguration(){
        var cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setAPIBuiltinEnabled(false);
        var bwb = new BeansWrapperBuilder(Configuration.VERSION_2_3_31);
        bwb.setExposureLevel(BeansWrapper.EXPOSE_NOTHING);
        bwb.setUseModelCache(false);
        cfg.setObjectWrapper(bwb.build());
        return cfg;
    }

    private static String getStringParam(Map params, String name, boolean required) throws TemplateException {
        Object val = params.get(name);
        if (val == null) {
            if (required) throw new TemplateException("Missing required param: " + name, null);
            return null;
        }
        if (!(val instanceof SimpleScalar)) {
            throw new TemplateException("Param " + name + " must be a string", null);
        }
        return ((SimpleScalar) val).getAsString();
    }

    private static String escapeHtml(String s) {
        return StringEscapeUtils.escapeHtml4(s);
    }

    private String getPresignedUrl(String path){
        return storagePresigner.sign(path);
    }

    @Override
    @SneakyThrows
    public RenderResult render(InputStream templateStream, OutputStream renderStream, Map<String, Object> emailParameters) {
        var cfg = getBaseConfiguration();
        cfg.setSharedVariable("loadImage", (TemplateDirectiveModel) (environment, params, templateModels, templateDirectiveBody) -> {
            var path = getStringParam(params, "path", true);
            var alt = getStringParam(params, "alt", false);
            var css = getStringParam(params, "class", false);
            var style = getStringParam(params, "style", false);
            var url = getPresignedUrl(path);
            var img = new StringBuilder("<img src=\"")
                    .append(url).append("\"");
            if (alt != null) {
                img.append(" alt=\"").append(escapeHtml(alt)).append("\"");
            }
            if (css != null) {
                img.append(" class=\"").append(escapeHtml(css)).append("\"");
            }
            if (style != null) {
                img.append(" style=\"").append(escapeHtml(style)).append("\"");
            }
            img.append("/>");

            environment.getOut().write(img.toString());
        });

        var reader = new InputStreamReader(templateStream, StandardCharsets.UTF_8);
        Template template;
        try {
            template = new Template("emailTemplate", reader, cfg);
        } catch (freemarker.core.ParseException e){
            throw new RequestException(400, e, "Email template contains syntax error");
        }
        var writer = new OutputStreamWriter(renderStream);
        HashMap<String, EmailImage> images;
        {
            var newParams = new HashMap<String, Object>();
            images = new HashMap<>();
            for (var entry : emailParameters.entrySet()){
                if (entry.getValue() instanceof EmailImage emailImage){
                    var fileExt = FilenameUtils.getExtension(emailImage.getFileName());
                    var randomizedName = fileExt.isEmpty() ? UUID.randomUUID().toString() : String.format("%s.%s", UUID.randomUUID(), fileExt);
                    newParams.put(entry.getKey(), "cid:" + randomizedName);
                    images.put(randomizedName, emailImage);
                    continue;
                }

                newParams.put(entry.getKey(), entry.getValue());
            }

            emailParameters = newParams;
        }
        template.process(emailParameters, writer);
        return new RenderResult(images);
    }
}
