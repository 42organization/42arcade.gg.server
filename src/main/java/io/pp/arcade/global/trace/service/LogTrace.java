package io.pp.arcade.global.trace.service;

import io.pp.arcade.global.trace.domain.TraceStatus;

public interface LogTrace {
    TraceStatus begin(String message);
    void end(TraceStatus status);
    void exception(TraceStatus status, Exception e);
}
