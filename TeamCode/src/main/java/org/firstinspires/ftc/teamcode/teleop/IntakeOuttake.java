package org.firstinspires.ftc.teamcode.teleop;

import android.widget.GridLayout;
import java.util.concurrent.TimeUnit;

public class IntakeOuttake {
    TelescopingArm arm;
    public Instructions instruction;
    public SpecificInstructions specificInstruction;
    public SpecificInstructions previousSpecificInstruction;
    private long previous_action = System.currentTimeMillis();
    private double waitTime = 1000;

    public IntakeOuttake(TelescopingArm arm) {
        this.arm = arm;
        instruction = Instructions.CLOSED;
        specificInstruction = SpecificInstructions.CLOSED;
    }

    public void reset(SpecificInstructions next) {
        previous_action = System.currentTimeMillis();
        waitTime = specificInstruction.time();
        specificInstruction = next;
    }

    public void reset(double time, SpecificInstructions next) {
        previous_action = System.currentTimeMillis();
        waitTime = time;
        specificInstruction = next;
    }

    public void update() {
        switch (instruction) {
            case CLOSED:
                switch (specificInstruction) {
                    case CLOSED:
                        arm.retractFully();
                        arm.pitchToIntake();
                        break;
                }
                break;
        }

        arm.setPitch();
        arm.setExtension();
    }

    public void setInstructions(Instructions instruction) {
        this.instruction = instruction;
    }

    public void setSpecificInstruction(SpecificInstructions specificInstruction) {
        this.specificInstruction = specificInstruction;
    }

    public enum Instructions {
        CLOSED
    }

    public enum SpecificInstructions {
        CLOSED(1000);


        private final int executionTime;

        SpecificInstructions(int executionTime) {
            this.executionTime = executionTime;
        }

        public int time() {
            return executionTime;
        }
    }
}