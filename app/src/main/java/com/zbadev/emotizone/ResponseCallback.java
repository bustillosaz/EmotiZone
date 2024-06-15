package com.zbadev.emotizone;

public interface ResponseCallback {
    void onResponse(String response);
    void onError(Throwable throwable);
}
