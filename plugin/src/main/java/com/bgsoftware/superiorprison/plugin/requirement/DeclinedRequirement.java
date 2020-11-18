package com.bgsoftware.superiorprison.plugin.requirement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class DeclinedRequirement {

    @Setter
    private String display;
    private Object value;
    private Object required;
}
