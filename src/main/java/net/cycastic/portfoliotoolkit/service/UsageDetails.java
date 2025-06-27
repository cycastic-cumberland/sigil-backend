package net.cycastic.portfoliotoolkit.service;

import org.springframework.lang.Nullable;

public interface UsageDetails {
    @Nullable Integer getProjectCount();
    @Nullable Integer getLacpCount();
    @Nullable Integer getAttachmentCount();
    @Nullable Long getPerAttachmentSize();
    @Nullable Long getAllAttachmentSize();
}
