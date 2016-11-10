package io.prime.sse.client;

public interface SseEventSourceHandler 
{
    void onConnect() throws Exception;
    void onMessage(String event, SseMessage message) throws Exception;
    void onError(Throwable t);
}
