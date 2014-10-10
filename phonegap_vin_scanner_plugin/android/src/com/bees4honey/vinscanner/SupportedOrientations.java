package com.bees4honey.vinscanner;

public enum SupportedOrientations {
    PORTRAIT(0, false),
    UPSIDE_DOWN(180, false),
    LANDSCAPE(270, true),
    LANDSCAPE_REVERSE(90, true);

    private final int angle;
    private final boolean isLandscape;

    private SupportedOrientations(int angle, boolean isLandscape) {
        this.angle = angle;
        this.isLandscape = isLandscape;
    }

    public int getAngle() {
        return angle;
    }

    public boolean isLandscape() {
        return isLandscape;
    }
}
