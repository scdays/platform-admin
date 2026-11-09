package com.vtc.platformadmin.domain.invocation.model.support;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 调用记录业务域（domain）解析，与 api_operation.domain / OpenAPI tags 对齐。
 */
public final class InvocationDomainSupport {

    public static final String DOMAIN_TASK = "TASK";
    public static final String DOMAIN_INSTANCE = "INSTANCE";
    public static final String DOMAIN_EXPORT = "EXPORT";
    public static final String DOMAIN_ARTIFACT = "ARTIFACT";
    public static final String DOMAIN_AUTH = "AUTH";
    public static final String DOMAIN_WEBHOOK = "WEBHOOK";

    private static final Set<String> TASK_OPERATIONS = new HashSet<>();
    private static final Set<String> INSTANCE_OPERATIONS = new HashSet<>();
    private static final Set<String> EXPORT_OPERATIONS = new HashSet<>();
    private static final Set<String> ARTIFACT_OPERATIONS = new HashSet<>();
    private static final Set<String> AUTH_OPERATIONS = new HashSet<>();
    private static final Set<String> WEBHOOK_OPERATIONS = new HashSet<>();

    private InvocationDomainSupport() {
    }

    public static String resolveDomain(String operationId, String resourceType) {
        // For now, since we don't have OpenApiOperations, we'll just return null
        return null;
    }

    public static List<String> operationIdsForDomain(String domain) {
        // For now, return empty list
        return Collections.emptyList();
    }
}
