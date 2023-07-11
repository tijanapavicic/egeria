/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.odpi.openmetadata.adminservices.configuration.properties.OMAGServerConfig;
import org.odpi.openmetadata.http.HttpHelper;
import org.odpi.openmetadata.platformservices.rest.SuccessMessageResponse;
import org.odpi.openmetadata.platformservices.server.OMAGServerOperationalServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication(
        scanBasePackages = {"org.odpi.openmetadata"}
)
//TODO: ADD JAVADOCS!!!
public class OMAGServer {

    public static final String OMAG_SERVER = "[OMAGServer]-";
    private static final Logger LOG = LoggerFactory.getLogger(OMAGServer.class);
    private final OMAGServerOperationalServices operationalServices;

    @Value("${startup.user:system}")
    String sysUser;

    @Value("${omag.server.config}")
    Resource omagServerConfigLocation;

    @Autowired
    private ObjectMapper objectMapper;
    private String serverName = null;
    private OMAGServerConfig serverConfig = null;


    public OMAGServer() {
        this.operationalServices = new OMAGServerOperationalServices();
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .listeners(new ApplicationListener<ApplicationEvent>() {
                    @Override
                    public void onApplicationEvent(ApplicationEvent applicationEvent) {
//                        LOG.info("[[ OMAGServer ]] >> LISTENER :: {}", applicationEvent.getClass().getCanonicalName());
//                        LOG.info("{}listener::{}", OMAG_SERVER, applicationEvent.getClass().getSimpleName());
                    }
                })
                .sources(OMAGServer.class).run(args);
    }


    @EventListener(ApplicationReadyEvent.class)
    private void onApplicationReadyEvent() {
        LOG.debug("{}{}-[READY FOR TRAFFIC]", OMAG_SERVER, serverName);
    }

    @EventListener(ApplicationStartedEvent.class)
    private void onApplicationStartedEvent() throws Exception {
        LOG.debug("{}[STARTED]", OMAG_SERVER);
        loadServerConfig();
    }

    @EventListener(ApplicationFailedEvent.class)
    private void onApplicationFailedEvent() {
        LOG.debug("{}[FAILED]", OMAG_SERVER);
    }

    @EventListener(ContextClosedEvent.class)
    private void onContextClosedEvent() {
        LOG.debug("{}[STOPPED]", OMAG_SERVER);

        if (serverName != null) {
            LOG.info("Application stopped, deactivating server {}", serverName);
            operationalServices.deactivateTemporarilyServerList(sysUser, List.of(serverName));
        }
    }

    private void activateOMAGServerUsingPlatformServices() {

        LOG.info("{}[ACTIVATION]-[STARTED] [for name] :: {}", OMAG_SERVER, serverName);

        if (serverConfig == null) {
            LOG.info("{}[ACTIVATION]-[FAILED] [cause] :: configuration is null.", OMAG_SERVER);
            throw new ApplicationContextException("[OMAG SERVER ACTIVATION]-[FAILED] [cause] :: configuration is null.");

       /*     TODO: Confirm if this is desired behaviour
             This is clearly invalid application state since the OMAG system cannot start
             without configuration. Throwing error will close application context and shut the application DOWN.*/
        }

        LOG.info("{}[ACTIVATION]-[STARTED] [request sent for server name] :: {}",OMAG_SERVER, serverName);

        SuccessMessageResponse response = operationalServices
                .activateWithSuppliedConfig(sysUser.trim(), serverConfig.getLocalServerName(), serverConfig);

        if (response == null) {
            LOG.info("{}[ACTIVATION]-[FAILED] [cause] :: response is null.", OMAG_SERVER);
            throw new ApplicationContextException("[OMAG SERVER ACTIVATION]-[FAILED] [cause] :: response is null.");
        }

        if (response.getRelatedHTTPCode() != 200) {
            LOG.error("{}[ACTIVATION]-[FAILED] [cause] :: response is NOT OK [200].", OMAG_SERVER);
            return;

/*            TODO: OMAG system start-up error handling and application readiness probe
             In most cases it is state caused by configuration problem and cannot be recovered at runtime
             Two options:
             1) Propagate the error further i.e. Runtime/ApplicationContextException which will cause context to be closed and application shut DOWN
             throw new ApplicationContextException(response.getExceptionErrorMessage());
             2) Do not propagate error, log the error message and set application ready state to FALSE
             this will keep the application UP and the operator will have to manually change the configuration
             and restart the application/container*/
        }

        if (response.getRelatedHTTPCode() == 200) {
            LOG.info("{}[ACTIVATION]-[SUCCESS] [started server for name] :: {}", OMAG_SERVER, serverConfig.getLocalServerName());
            //TODO: Mark the application state as ready
            // i.e. set application ready state to TRUE
        }

    }

    private void loadServerConfig() {


        if (omagServerConfigLocation == null) {
            LOG.info("{}[CONFIG]-[FAILED] [cause] :: file is null", OMAG_SERVER);
            //TODO: Confirm if this is desired behaviour (configuration is null)
            // This is clearly invalid application state since the OMAG system cannot start without configuration.
            // Throwing error will close application context and shut the application DOWN.
            throw new ApplicationContextException(String.format("{}-[CONFIG]-[FAILED] [cause] :: file is null", OMAG_SERVER));
        }
        try {
            LOG.info("{}[CONFIG]-[STARTED] [location] :: {}", OMAG_SERVER, omagServerConfigLocation.getFile());
            LOG.trace("{}[CONFIG]-[STARTED] [path] :: {}", OMAG_SERVER, Files.readString(Path.of(omagServerConfigLocation.getFile().getPath())));
            serverConfig = objectMapper.reader().readValue(omagServerConfigLocation.getFile(), OMAGServerConfig.class);
            serverName = serverConfig.getLocalServerName();
            LOG.info("{}[CONFIG]-[SUCCESS] [loaded configuration document for OMAG server name] :: {}", OMAG_SERVER, serverName);

        } catch (IOException e) {
            LOG.error("{}[CONFIG]-[FAILED] [Exception while loading OMAG server configuration] [cause] :: {}", OMAG_SERVER, e.getMessage());
            //TODO: Confirm if this is desired behaviour (configuration is null)
            // Same as in the case above.
            throw new ApplicationContextException(
                    String.format("{}[CONFIG]-[FAILED] [Exception while loading OMAG server configuration] [cause] :: {}", OMAG_SERVER, e.getMessage()));
        }
    }

    @Component
    public class OMAGServerStartup implements ApplicationRunner {
        @Override
        public void run(ApplicationArguments args) {
            LOG.debug("{}[RUN] Application runner", OMAG_SERVER);
            activateOMAGServerUsingPlatformServices();
        }
    }
}
