package net.cycastic.sigil.service;

import jakarta.annotation.Nullable;

public interface UsageDetails {
    @Nullable Integer getProjectCount();
    @Nullable Integer getLacpCount();
    @Nullable Integer getAttachmentCount();
    @Nullable Long getPerAttachmentSize();
    @Nullable Long getAllAttachmentSize();
}
