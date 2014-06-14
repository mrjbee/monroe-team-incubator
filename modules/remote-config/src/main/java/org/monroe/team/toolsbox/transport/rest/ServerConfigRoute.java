package org.monroe.team.toolsbox.transport.rest;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class ServerConfigRoute  extends SpringRouteBuilder{

    private final Properties props= new Properties();
    @Override
    public void configure() throws Exception {

        from("restlet:/server/moon/sleepminutes").transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("sleepminutes","60");
            }
        });

        from("restlet:/server/moon/sleepminutes?restletMethod=post").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                String minutes = exchange.getIn().getBody(String.class);
                props.setProperty("sleepminutes",minutes);
            }
        }).transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("sleepminutes","60");
            }
        });

        from("restlet:/server/moon/status").transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("status","NaN");
            }
        });

        from("restlet:/server/moon/status?restletMethod=post").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                String status = exchange.getIn().getBody(String.class);
                props.setProperty("status",status);
            }
        }).transform(new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) props.getProperty("status","NaN");
            }
        });
    }
}
