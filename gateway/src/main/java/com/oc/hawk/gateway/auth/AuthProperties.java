package com.oc.hawk.gateway.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;


@Data
public class AuthProperties {
    private Set<String> whiteList;
}
