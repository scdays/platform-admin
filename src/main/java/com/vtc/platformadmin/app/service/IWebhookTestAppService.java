package com.vtc.platformadmin.app.service;

public interface IWebhookTestAppService {

    void receive(String rawBody, String signature, String timestamp);
}
