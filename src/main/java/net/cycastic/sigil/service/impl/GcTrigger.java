package net.cycastic.sigil.service.impl;

import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

@Component
public class GcTrigger {
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        System.gc();
    }
}
