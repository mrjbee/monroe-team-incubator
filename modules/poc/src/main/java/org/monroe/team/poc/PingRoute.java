package org.monroe.team.poc;

import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class PingRoute extends SpringRouteBuilder {
    @Override
    public void configure() throws Exception {
        from("restlet:/version").transform(simple("Will be soon"));
    }
}
