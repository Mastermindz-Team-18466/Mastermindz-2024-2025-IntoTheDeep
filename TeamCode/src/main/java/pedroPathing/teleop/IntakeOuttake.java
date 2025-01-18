package pedroPathing.teleop;

import android.widget.GridLayout;
import java.util.concurrent.TimeUnit;

public class IntakeOuttake {
    public static TelescopingArm arm;
    public static Claw claw;
    Differential diffy;
    public Instructions instruction;
    public SpecificInstructions specificInstruction;
    public SpecificInstructions previousSpecificInstruction;
    private long previous_action = System.currentTimeMillis();
    private double waitTime = 1000;
    public static boolean closed_zero_out = true;

    public IntakeOuttake(TelescopingArm arm, Claw claw, Differential diffy) {
        this.arm = arm;
        this.claw = claw;
        this.diffy = diffy;

        instruction = Instructions.CLOSED;
        specificInstruction = SpecificInstructions.CLOSED;
        closed_zero_out = true;
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
                        diffy.close();
                        claw.close();
                        break;
                }
                break;
            case PITCH_DOWN_CLOSE:
                switch (specificInstruction) {
                    case PITCH_INTAKE:
                        arm.pitchTo(25);
                        reset(SpecificInstructions.CLOSE_CLAW);
                        break;
                    case CLOSE_CLAW:
                        if (System.currentTimeMillis() - previous_action > 250) {
                            claw.close();
                        }
                        break;
                }
                break;
            case EXTEND_TO_ONE:
                switch (specificInstruction) {
                    case INTAKE_EXTENSION:
                        arm.pitchTo(25);
                        diffy.intake();
                        claw.open();
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 250) {
                            arm.extendTo(-1200);
                        }
                        break;
                }
                break;
            case EXTEND_TO_ONE_SPEC:
                switch (specificInstruction) {
                    case INTAKE_EXTENSION:
                        arm.pitchTo(25);
                        arm.extendTo(-880);
                        claw.open();
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 450) {
                            diffy.setPosition(0.75, 0.05);
                        }
                        break;
                }
                break;
            case EXTEND_TO_TWO:
                switch (specificInstruction) {
                    case INTAKE_EXTENSION:
                        arm.pitchTo(25);
                        diffy.intake();
                        claw.open();
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 250) {
                            arm.extendTo(-1300);
                        }
                        break;
                }
                break;
            case EXTEND_TO_TWO_SPEC:
                switch (specificInstruction) {
                    case INTAKE_EXTENSION:
                        arm.pitchTo(25);
                        diffy.setPosition(0.75, 0.05);
                        claw.open();
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 250) {
                            arm.extendTo(-880);
                        }
                        break;
                }
                break;
            case EXTEND_TO_DROP:
                switch (specificInstruction) {
                    case INTAKE_EXTENSION:
                        arm.extendTo(-880);
                        break;
                }
                break;
            case PRE_EXTEND_TWO:
                switch (specificInstruction) {
                    case INTAKE_EXTENSION:
                        arm.extendTo(-880);
                        break;
                }
                break;
            case EXTEND_TO_THREE:
                switch (specificInstruction) {
                    case INTAKE_EXTENSION:
                        arm.pitchTo(25);
                        diffy.intake();
                        claw.open();
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 250) {
                            arm.extendTo(-1320);
                        }
                        break;
                }
                break;
            case INTAKE:
                switch (specificInstruction) {
                    case INTAKE_EXTENSION:
                        arm.pitchTo(0);
                        arm.extendTo(-500);
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 250) {
                            diffy.intake();
                            claw.open();
                        }
                        break;
                }
                break;
            case SPECIMAN_INTAKE:
                switch (specificInstruction) {
                    case INTAKE_EXTENSION:
                        arm.pitchToSpecimenIntake();
                        arm.extendTo(-80);
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 250) {
                            diffy.mid();
                            claw.open();
                        }
                        break;
                }
                break;
            case HOLD:
                switch (specificInstruction) {
                    case MAX_RETRACT:
                        diffy.setPosition(0.2, 0.8);
                        arm.retractFully();
                        reset(SpecificInstructions.PITCH_INTAKE);
                        break;
                    case PITCH_INTAKE:
                        if (arm.extensionLeft.getCurrentPosition() > -150 && System.currentTimeMillis() - previous_action > 250) {
                            arm.retractFully();
                            arm.pitchToIntake();
                        }
                        break;
                }
                break;
            case DOWN_HOLD:
                switch (specificInstruction) {
                    case MAX_RETRACT:
                        arm.retractFully();
                        reset(SpecificInstructions.PITCH_INTAKE);
                        break;
                    case PITCH_INTAKE:
                        if (arm.extensionLeft.getCurrentPosition() > -150 && System.currentTimeMillis() - previous_action > 250) {
                            arm.retractFully();
                            arm.pitchToIntake();
                            reset(SpecificInstructions.DEPO_DIFFY);
                        }
                        break;
                    case DEPO_DIFFY:
                        if (arm.pitch.getCurrentPosition() < 1000) {
                            diffy.setPosition(0.2, 0.8);
                        }
                        break;
                }
                break;
            case DEPOSIT:
                switch (specificInstruction) {
                    case PITCH_DEPOSIT:
                        arm.pitchToDeposit();
                        diffy.intake();
                        reset(SpecificInstructions.MAX_EXTEND);
                        break;
                    case MAX_EXTEND:
                        if (System.currentTimeMillis() - previous_action > waitTime && arm.pitch.getCurrentPosition() > 1800) {
                            arm.extendFully();
                            reset(SpecificInstructions.DEPO_DIFFY);
                        }
                        break;
                    case DEPO_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 500 && arm.extensionLeft.getCurrentPosition() < -500) {
                            diffy.deposit();
                        }
                        break;
                }
                break;
            case SPECIMAN_DEPOSIT:
                switch (specificInstruction) {
                    case PITCH_DEPOSIT:
                        diffy.specimanDeposit();
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
            case FRONT_SPECIMAN_DEPOSIT:
                switch (specificInstruction) {
                    case PITCH_DEPOSIT:
                        diffy.setPosition(0.725, 0.825);
                        arm.pitchTo(750);
                        reset(SpecificInstructions.SPECIMAN_EXTEND);
                        break;
                    case SPECIMAN_EXTEND:
                        if (arm.pitch.getCurrentPosition() > 750) {
                            arm.extendTo(-675);
                        }
                        break;
                }
                break;
            case SPECIMAN_DEPOSIT_DOWN:
                switch (specificInstruction) {
                    case SPECIMAN_EXTEND:
                        diffy.specimanDown();
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
                        if (System.currentTimeMillis() - previous_action > 250 && claw.claw.getPosition() == 0) {
                            diffy.intake();
                        }
                        break;
                }
                break;
            case SPEC_OPEN_CLAW:
                switch (specificInstruction) {
                    case OPEN_CLAW:
                        claw.open();
                        reset(SpecificInstructions.INTAKE_DIFFY);
                        break;
                    case INTAKE_DIFFY:
                        if (System.currentTimeMillis() - previous_action > 250 && claw.claw.getPosition() == 0) {
                            diffy.deposit();
                        }
                        break;
                }
                break;
            case HORIZ_DIFFY:
                switch (specificInstruction) {
                    case INTAKE_DIFFY:
                        diffy.setPosition(0.7, 0);
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

            case AUTO_CLOSE_CLAW:
                switch (specificInstruction) {
                    case CLOSE_CLAW:
                        claw.close();
                        break;
                }
                break;
            
            case CHANGE_DIFFY:
                switch (specificInstruction) {
                    case UP:
                        diffy.setPosition(diffy.left_position - 0.005, diffy.right_position + 0.005);
                        break;
                    case DOWN:
                        diffy.setPosition(diffy.left_position + 0.005, diffy.right_position - 0.005);
                        break;
                    case LEFT:
                        diffy.setPosition(diffy.left_position - 0.005, diffy.right_position - 0.005);
                        break;
                    case RIGHT:
                        diffy.setPosition(diffy.left_position + 0.005, diffy.right_position + 0.005);
                        break;
                }
                break;
        }

        if (!closed_zero_out) {
            arm.setPitch();
            arm.setExtension();
        } else {
            arm.extensionLeft.setPower(0);
            arm.extensionRight.setPower(0);
            arm.pitch.setPower(0);
        }
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
        SPECIMAN_DEPOSIT_DOWN, HOLD, CHANGE_DIFFY, SPECIMAN_INTAKE, PITCH_DOWN_CLOSE, HORIZ_DIFFY, EXTEND_TO, EXTEND_TO_ONE, EXTEND_TO_TWO, EXTEND_TO_THREE, AUTO_CLOSE_CLAW, FRONT_SPECIMAN_DEPOSIT, EXTEND_TO_ONE_SPEC, SPEC_OPEN_CLAW, EXTEND_TO_TWO_SPEC, EXTEND_TO_DROP, PRE_EXTEND_TWO, DOWN_HOLD;
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
        INTAKE_EXTENSION(1000),
        INTAKE_DIFFY(1000),
        UP(1000), DOWN(1000), LEFT(1000), RIGHT(1000), DEPO_DIFFY(1000);


        private final int executionTime;

        SpecificInstructions(int executionTime) {
            this.executionTime = executionTime;
        }

        public int time() {
            return executionTime;
        }
    }
}