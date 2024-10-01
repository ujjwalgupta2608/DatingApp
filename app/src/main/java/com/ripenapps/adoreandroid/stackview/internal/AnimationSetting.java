package com.ripenapps.adoreandroid.stackview.internal;

import android.view.animation.Interpolator;

import com.ripenapps.adoreandroid.stackview.Direction;


public interface AnimationSetting {
    Direction getDirection();
    int getDuration();
    Interpolator getInterpolator();
}
