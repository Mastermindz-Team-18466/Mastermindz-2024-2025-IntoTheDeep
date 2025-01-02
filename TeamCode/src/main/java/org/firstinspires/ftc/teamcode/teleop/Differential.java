package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Differential {
    static final double[] intake = new double[]{0.85, 0.15};
    static final double[] deposit = new double[]{0.4, 0.6};
    static final double[] specimanIntake = new double[]{0.55, 0.45};

    public static Servo left;
    public static Servo right;
    public static double left_position = 0;
    public static double right_position = 0;
    private static final double SERVO_STEP = 0.05;

    public Differential(HardwareMap hardwareMap) {
        left = hardwareMap.get(Servo.class, "intakeLeft");
        right = hardwareMap.get(Servo.class, "intakeRight");
    }

    public static void setDifferential() {
        left.setPosition(left_position);
        right.setPosition(right_position);
    }

    public static void deposit() {
        left_position = deposit[0];
        right_position = deposit[1];
    }

    public static void intake() {
        left_position = intake[0];
        right_position = intake[1];
    }

    public static void specimenIntake() {
        left_position = specimanIntake[0];
        right_position = specimanIntake[1];
    }

    public static void setPosition(double left, double right) {
        left_position = left;
        right_position = right;
    }
}