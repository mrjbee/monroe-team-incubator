package org.monroe.team.notification.bridge.strategies;

import java.util.UUID;

public class IdGeneratorStrategy {

    final public String generateId(String seed){
        return UUID.randomUUID().toString()+":"+seed;
    }

}
