package net.cycastic.portfoliotoolkit.service;

import net.cycastic.portfoliotoolkit.domain.model.EmailParameter;

import java.io.InputStream;
import java.io.OutputStream;

public interface EmailTemplateEngine {
    void render(InputStream templateStream,
                OutputStream renderStream,
                EmailParameter[] emailParameters);
}
