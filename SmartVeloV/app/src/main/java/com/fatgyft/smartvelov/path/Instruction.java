package com.fatgyft.smartvelov.path;

/**
 * Created by Felipe on 29-Apr-15.
 */
import android.util.Pair;

import java.util.ArrayList;

public class Instruction {

    public static final int TURN_SHARP_LEFT = -3;
    public static final int TURN_LEFT = -2;
    public static final int TURN_SLIGHT_LEFT = -1;
    public static final int CONTINUE_ON_STREET = 0;
    public static final int TURN_SLIGHT_RIGHT = 1;
    public static final int TURN_RIGHT = 2;
    public static final int TURN_SHARP_RIGHT = 3;
    public static final int FINISH = 4;
    public static final int VIA_REACHED = 5;
    public static final int USE_ROUNDABOUT = 6;

    Integer sign;
    Pair<Integer, Integer> interval;


    public Instruction(Integer sign, Pair<Integer, Integer> interval) {
        this.interval = interval;
        this.sign = sign;
    }
}
