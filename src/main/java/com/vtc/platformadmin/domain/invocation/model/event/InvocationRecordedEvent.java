package com.vtc.platformadmin.domain.invocation.model.event;

import com.vtc.asset.security.platform.eventbus.api.DomainEvent;
import com.vtc.asset.security.platform.eventbus.api.EventContextAware;

/**
 * 调用记录事件。
 * <p>
 * 由 partner-gateway 在请求完成时通过 {@code publishAsync} 发布，
 * platform-admin 消费后写入 api_invocation。
 *
 * @author asset-security
 */
public class InvocationRecordedEvent implements DomainEvent, EventContextAware {

    private String invocationId;
    private String requestId;
    private String partnerId;
    private String clientId;
    private String apiCode;
    private String method;
    private String path;
    private String routeMode;
    private String targetService;
    private Long startTime;
    private Long endTime;
    private Long costMillis;
    private Integer statusCode;
    private Integer httpStatus;
    private String clientIp;
    private String errorCode;
    private String errorMessage;
    private String traceId;

    @Override
    public String eventType() {
        return "open-platform.invocation.recorded";
    }

    @Override
    public String idempotentKey() {
        return "invocation:" + invocationId;
    }

    @Override
    public String tenantType() {
        return "partner";
    }

    @Override
    public String tenantId() {
        return partnerId;
    }

    @Override
    public String subjectType() {
        return "invocation";
    }

    @Override
    public String subjectId() {
        return invocationId;
    }

    // ======== getters / setters ========

    public String getInvocationId() {
        return invocationId;
    }

    public void setInvocationId(String invocationId) {
        this.invocationId = invocationId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRouteMode() {
        return routeMode;
    }

    public void setRouteMode(String routeMode) {
        this.routeMode = routeMode;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getCostMillis() {
        return costMillis;
    }

    public void setCostMillis(Long costMillis) {
        this.costMillis = costMillis;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}