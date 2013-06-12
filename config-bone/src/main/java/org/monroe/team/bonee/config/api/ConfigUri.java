package org.monroe.team.bonee.config.api;

import org.monroe.team.bonee.core.utils.Should;

import java.util.List;

/**
 * User: MisterJBee
 * Date: 6/12/13 Time: 12:34 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
final public class ConfigUri {

    private final String[] sectionPath;

    public final ConfigUri uri(String... sections){
        Should.shouldBeTrue("Sections path could be empty (or null)",sections != null, sections.length != 0);
        for (String section:sections){
            Should.shouldBeTrue("Should contain no special characters",
                    !section.contains("/"),
                    !section.contains("\\"));
        }
        return new ConfigUri(sectionPath);
    }

    ConfigUri(String[] sectionPath) {
        this.sectionPath = sectionPath;
    }

    public String[] getSectionPath() {
        return sectionPath;
    }

    public String asPlaintString() {
        return convertToString(this);
    }

    private String convertToString(ConfigUri configUri) {
        StringBuilder builder = new StringBuilder(configUri.getSectionPath().length);
        for(String section: configUri.getSectionPath()){
            builder.append(section);
        }
        return builder.toString();
    }

}
