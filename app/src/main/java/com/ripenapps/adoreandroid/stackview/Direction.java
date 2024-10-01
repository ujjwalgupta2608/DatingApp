package com.ripenapps.adoreandroid.stackview;

import java.util.Arrays;
import java.util.List;

public enum Direction {
    Left,
    Right,
    Top,
    Bottom;

    public static final List<Direction> HORIZONTAL = Arrays.asList(Direction.Left, Direction.Right/*,Direction.Top*/);
    public static final List<Direction> VERTICAL = Arrays.asList(Direction.Top,Direction.Bottom);
    public static final List<Direction> FREEDOM = Arrays.asList(Direction.values());
}
