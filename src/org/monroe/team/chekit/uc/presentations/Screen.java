package org.monroe.team.chekit.uc.presentations;

public enum Screen {
    STARTUP("slide_start.fxml"),
    RUNNER("slide_check_run.fxml");


    public final String layoutName;

    Screen(String layoutName) {
        this.layoutName = layoutName;
    }
}
