package com.assetvault.service;

import org.springframework.stereotype.Service;

@Service
class LoggingProbeService {

    public String echo(String value) {
        return value.toUpperCase();
    }

    public void fail() {
        throw new IllegalStateException("boom");
    }
}
