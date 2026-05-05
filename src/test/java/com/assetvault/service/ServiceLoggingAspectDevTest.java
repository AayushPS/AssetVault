package com.assetvault.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.assetvault.config.ServiceLoggingAspect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(ServiceLoggingAspectTestConfig.class)
@ActiveProfiles("dev")
@ExtendWith(OutputCaptureExtension.class)
class ServiceLoggingAspectDevTest {
    @Autowired
    private LoggingProbeService service;

    private Logger serviceLogger;
    private Level originalLevel;

    @BeforeEach
    void enableDebugLogging() {
        serviceLogger = (Logger) LoggerFactory.getLogger(LoggingProbeService.class);
        originalLevel = serviceLogger.getLevel();
        serviceLogger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void restoreLogging() {
        serviceLogger.setLevel(originalLevel);
    }

    @Test
    void logsServiceMethodEntryAndExitWhenDevProfileIsActive(CapturedOutput output) {
        assertThat(service.echo("asset")).isEqualTo("ASSET");

        assertThat(output)
                .contains("Entering LoggingProbeService.echo")
                .contains("Exiting LoggingProbeService.echo after");
    }

    @Test
    void logsExceptionalServiceExitWhenDevProfileIsActive(CapturedOutput output) {
        assertThatThrownBy(service::fail)
                .isInstanceOf(IllegalStateException.class);

        assertThat(output)
                .contains("Entering LoggingProbeService.fail")
                .contains("Exiting LoggingProbeService.fail with IllegalStateException after");
    }
}

@SpringJUnitConfig(ServiceLoggingAspectTestConfig.class)
@ActiveProfiles("prod")
@ExtendWith(OutputCaptureExtension.class)
class ServiceLoggingAspectProdTest {
    @Autowired
    private LoggingProbeService service;

    private Logger serviceLogger;
    private Level originalLevel;

    @BeforeEach
    void enableDebugLogging() {
        serviceLogger = (Logger) LoggerFactory.getLogger(LoggingProbeService.class);
        originalLevel = serviceLogger.getLevel();
        serviceLogger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void restoreLogging() {
        serviceLogger.setLevel(originalLevel);
    }

    @Test
    void skipsServiceMethodEntryAndExitWhenDevProfileIsInactive(CapturedOutput output) {
        assertThat(service.echo("asset")).isEqualTo("ASSET");

        assertThat(output)
                .doesNotContain("Entering LoggingProbeService.echo")
                .doesNotContain("Exiting LoggingProbeService.echo");
    }
}

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({ServiceLoggingAspect.class, LoggingProbeService.class})
@Profile({"dev", "prod"})
class ServiceLoggingAspectTestConfig {
}
