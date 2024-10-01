package com.ripenapps.adoreandroid.stackview;

public enum Duration {
    Fast(100),
    Normal(700),
    Slow(500);

    public final int duration;

    Duration(int duration) {
        this.duration = duration;
    }

    public static Duration fromVelocity(int velocity) {
        if (velocity < 1000) {
            return Slow;
        } else if (velocity < 5000) {
            return Normal;
        }
        return Fast;
    }
}
