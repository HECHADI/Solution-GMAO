package com.gmao.event;

import com.gmao.entity.Intervention;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Événement métier déclenché lorsqu'une intervention est clôturée (statut REALISE).
 */
@Getter
public class InterventionRealiseeEvent extends ApplicationEvent {
    
    private final Intervention intervention;

    public InterventionRealiseeEvent(Object source, Intervention intervention) {
        super(source);
        this.intervention = intervention;
    }
}
