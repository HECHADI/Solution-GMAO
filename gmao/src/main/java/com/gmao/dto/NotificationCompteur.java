package com.gmao.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationCompteur {
    private int total;
    private int critique;
    private int attention;
}
