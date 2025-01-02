package org.firstinspires.ftc.teamcode.teleop;

import android.widget.GridLayout;
import java.util.concurrent.TimeUnit;

public class IntakeOuttake {
    TelescopingArm arm;
    Claw claw;
    Differential diffy;
    public Instructions instruction;
    public SpecificInstructions specificInstruction;
    public SpecificInstructions previousSpecificInstruction;
    private long previous_action = System.currentTimeMillis();
    private double waitTime = 1000;

    public IntakeOuttake(TelescopingArm arm, Claw claw, Differential diffy) {
        this.arm = arm;
        this.claw = claw;
        this.diffy = diffy;

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
                        diffy.deposit();
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
                    case INTAKE_EXTENSION:
                        arm.extendTo(500);
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 250 && arm.extensionLeft.getCurrentPosition() > 400) {
                            diffy.intake();
                            claw.open();
                        }
                        break;
                }
                break;
            case HOLD:
                switch (specificInstruction) {
                    case MAX_RETRACT:
                        arm.retractFully();
                        diffy.setPosition(0.2, 0.8);
                        reset(SpecificInstructions.PITCH_INTAKE);
                        break;
                    case PITCH_INTAKE:
                        if (System.currentTimeMillis() - previous_action > waitTime && arm.extensionLeft.getCurrentPosition() < 100) {
                            arm.pitchToIntake();
                        }
                        break;
                }
                break;
            case DEPOSIT:
                switch (specificInstruction) {
                    case PITCH_DEPOSIT:
                        diffy.deposit();
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
                        diffy.deposit();
                        arm.pitchToSpecimen();
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
                }
                break;
            case OPEN_CLAW:
                switch (specificInstruction) {
                    case OPEN_CLAW:
                        claw.open();
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 100 && claw.claw.getPosition() == 0) {
                            diffy.intake();
                        }
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
        diffy.setDifferential();
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
        SPECIMAN_DEPOSIT_DOWN, HOLD;
    }

    public enum SpecificInstructions {
        CLOSED(1000),
        PITCH_DEPOSIT(1000),
        MAX_EXTEND(1000),
        PITCH_INTAKE(1000),
        MAX_RETRACT(1000),
        OPEN_CLAW(500),
        CLOSE_CLAW(500),
        SPECIMAN_EXTEND(1000),
        INTAKE_EXTENSION(1000), INTAKE_DIFFY(1000);


        private final int executionTime;

        SpecificInstructions(int executionTime) {
            this.executionTime = executionTime;
        }

        public int time() {
            return executionTime;
        }
    }
}