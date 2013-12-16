package org.monroe.team.notification.bridge.services;

import java.util.UUID;

public class IdGenerator {

    final public String generateId(String seed){
        return UUID.randomUUID().toString()+":"+seed;
    }

}
