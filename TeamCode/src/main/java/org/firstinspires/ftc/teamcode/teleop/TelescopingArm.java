package org.firstinspires.ftc.teamcode.teleop;

import static java.lang.Math.max;

import com.arcrobotics.ftclib.controller.PIDFController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class TelescopingArm {
    private static PIDFController pitchController;
    private static final double pitchP = 0.01, pitchI = 0, pitchD = 0.0000001;
    private static final double pitchF = 0.00004;
    private static final int pitchDepositBound = 3000;
    private static final int pitchIntakeBound = 300;
    private static final int pitchIntakePosition = 300;
    private static final int pitchDepositPosition = 2000;
    private static final int pitchSpecimenPosition = 1000;

    private static PIDFController extensionController;
    private static final double extensionP = 0, extensionI = 0, extensionD = 0;
    private static final double extensionF = 0;
    private static final double retractedBound = 0;
    private static final double extendedBound = 0;
    private static final double extensionRetractedPosition = 0;
    private static final double extensionExtendedPosition = 0;

    public static DcMotorEx pitch;
    public static DcMotorEx extensionLeft;
    public static DcMotorEx extensionRight;

    public static double pitchTargetPosition = 0;
    public static double extensionTargetPosition = 0;
    public static double extensionOffset = 0;

    public TelescopingArm(HardwareMap hardwareMap) {
        pitchController = new PIDFController(pitchP, pitchI, pitchD, pitchF);
        extensionController = new PIDFController(extensionP, extensionI, extensionD, extensionF);

        pitch = hardwareMap.get(DcMotorEx.class, "pitch");
        extensionLeft = hardwareMap.get(DcMotorEx.class, "firststring");
        extensionRight = hardwareMap.get(DcMotorEx.class, "secondstring");

        pitch.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        pitch.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        pitch.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        extensionLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        extensionRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        extensionLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        extensionRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        extensionLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        extensionRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        pitchTargetPosition = pitch.getCurrentPosition();
        extensionTargetPosition = extensionLeft.getCurrentPosition();

        pitch.setDirection(DcMotor.Direction.FORWARD);
        extensionLeft.setDirection(DcMotor.Direction.FORWARD);
        extensionRight.setDirection(DcMotor.Direction.REVERSE);
    }

    public static void setPitch() {
        if (pitchTargetPosition <= pitchIntakeBound) {
            pitchTargetPosition = pitchIntakeBound;
        }
        if (pitchTargetPosition >= pitchDepositBound) {
            pitchTargetPosition = pitchDepositBound;
        }

        pitchController.setPIDF(pitchP, pitchI, pitchD, pitchF);
        double pitchCurrentPosition = pitch.getCurrentPosition();
        double power = pitchController.calculate(pitchCurrentPosition, pitchTargetPosition) + pitchF;
        pitch.setPower(power);
    }

    public static void pitchTo(double targetPosition) {
        pitchTargetPosition = targetPosition;
    }

    public static void pitchToDeposit() {
        pitchTargetPosition = pitchDepositPosition;
    }

    public static void pitchToIntake() {
        pitchTargetPosition = pitchIntakePosition;
    }

    public static void pitchToSpecimen() {
        pitchTargetPosition = pitchSpecimenPosition;
    }

    public static void setExtension() {
        if (extensionTargetPosition + extensionOffset <= retractedBound) {
            extensionTargetPosition = retractedBound;
        }
        if (extensionTargetPosition + extensionOffset >= extendedBound) {
            extensionTargetPosition = extendedBound;
        }

        extensionController.setPIDF(extensionP, extensionI, extensionD, extensionF);
        double extensionCurrentPosition = extensionLeft.getCurrentPosition();
        double power = extensionController.calculate(extensionCurrentPosition + extensionOffset, extensionTargetPosition) + extensionF;
        extensionLeft.setPower(power);
        extensionRight.setPower(power);
    }

    public static void extendTo(double targetPosition) {
        extensionTargetPosition = targetPosition;
    }

    public static void extendFully() {
        extensionTargetPosition = extensionExtendedPosition;
    }

    public static void retractFully() {
        extensionTargetPosition = extensionRetractedPosition;
    }
}
