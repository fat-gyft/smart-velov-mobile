package com.fatgyft.smartvelov.path;

/**
 * Created by Felipe on 29-Apr-15.
 */
import java.util.ArrayList;

public class Instruction {

    Integer sign;

    ArrayList<Integer> interval;

    public Instruction(Integer sign, ArrayList<Integer> interval) {
        this.interval = interval;
        this.sign = sign;

    }
}
