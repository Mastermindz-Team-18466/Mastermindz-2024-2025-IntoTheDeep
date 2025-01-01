package org.firstinspires.ftc.teamcode.teleop;

import android.widget.GridLayout;
import java.util.concurrent.TimeUnit;

public class IntakeOuttake {
    TelescopingArm arm;
    Claw claw;
    public Instructions instruction;
    public SpecificInstructions specificInstruction;
    public SpecificInstructions previousSpecificInstruction;
    private long previous_action = System.currentTimeMillis();
    private double waitTime = 1000;

    public IntakeOuttake(TelescopingArm arm, Claw claw) {
        this.arm = arm;
        this.claw = claw;

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
                    case MAX_RETRACT:
                        arm.retractFully();
                        reset(SpecificInstructions.PITCH_INTAKE);
                        break;
                    case PITCH_INTAKE:
                        if (System.currentTimeMillis() - previous_action > waitTime && arm.extensionLeft.getCurrentPosition() < 100) {
                            arm.pitchToIntake();
                        }
                        break;
                }
                break;
            case INTAKE:
                switch (specificInstruction) {
                    case MAX_RETRACT:
                        arm.retractFully();
                        reset(SpecificInstructions.PITCH_INTAKE);
                        break;
                    case PITCH_INTAKE:
                        if (System.currentTimeMillis() - previous_action > waitTime && arm.extensionLeft.getCurrentPosition() < 100) {
                            arm.pitchToIntake();
                            claw.open();
                        }
                        break;
                }
                break;
            case DEPOSIT:
                switch (specificInstruction) {
                    case PITCH_DEPOSIT:
                        arm.pitchToDeposit();
                        reset(SpecificInstructions.MAX_EXTEND);
                        break;
                    case MAX_EXTEND:
                        if (System.currentTimeMillis() - previous_action > waitTime && arm.pitch.getCurrentPosition() > 1800) {
                            arm.extendFully();
                        }
                        break;
                }
                break;
            case SPECIMAN_DEPOSIT:
                switch (specificInstruction) {
                    case PITCH_DEPOSIT:
                        arm.pitchToDeposit();
                        reset(SpecificInstructions.SPECIMAN_EXTEND);
                        break;
                    case SPECIMAN_EXTEND:
                        if (System.currentTimeMillis() - previous_action > waitTime && arm.pitch.getCurrentPosition() > 1800) {
                            arm.extendSpeciman();
                        }
                        break;
                }
                break;
            case SPECIMAN_DEPOSIT_DOWN:
                switch (specificInstruction) {
                    case SPECIMAN_EXTEND:
                        arm.extendSpecimanDown();
                        break;
                    case OPEN_CLAW:
                        if (System.currentTimeMillis() - previous_action > 1000) {
                            arm.extendSpeciman();
                        }
                        break;
                }
                break;
            case OPEN_CLAW:
                switch (specificInstruction) {
                    case OPEN_CLAW:
                        claw.open();
                        break;
                }
                break;

            case CLOSE_CLAW:
                switch (specificInstruction) {
                    case CLOSE_CLAW:
                        claw.close();
                        break;
                }
                break;
        }

        arm.setPitch();
        arm.setExtension();
        claw.setClaw();
    }

    public void setInstructions(Instructions instruction) {
        this.instruction = instruction;
    }

    public void setSpecificInstruction(SpecificInstructions specificInstruction) {
        this.specificInstruction = specificInstruction;
    }

    public enum Instructions {
        CLOSED,
        DEPOSIT,
        INTAKE,
        OPEN_CLAW,
        CLOSE_CLAW,
        SPECIMAN_DEPOSIT,
        SPECIMAN_DEPOSIT_DOWN;
    }

    public enum SpecificInstructions {
        CLOSED(1000),
        PITCH_DEPOSIT(1000),
        MAX_EXTEND(1000),
        PITCH_INTAKE(1000),
        MAX_RETRACT(1000),
        OPEN_CLAW(500),
        CLOSE_CLAW(500),
        SPECIMAN_EXTEND(1000);


        private final int executionTime;

        SpecificInstructions(int executionTime) {
            this.executionTime = executionTime;
        }

        public int time() {
            return executionTime;
        }
    }
}