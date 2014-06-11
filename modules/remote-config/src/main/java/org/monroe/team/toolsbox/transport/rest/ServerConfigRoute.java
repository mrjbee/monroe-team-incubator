package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ServerConfigRoute  extends SpringRouteBuilder{

    @Override
    public void configure() throws Exception {
        from("restlet:/server/{server_id}/awake").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                System.out.println(exchange);
            }
        }).transform(header("server_id"));
    }
}
