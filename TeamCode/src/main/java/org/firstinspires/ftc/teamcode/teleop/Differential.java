package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Differential {
    static final double[] down = new double[]{0.85, 0.15};
    static final double[] up = new double[]{0.4, 0.6};
    static final double[] mid = new double[]{0.55, 0.45};

    public static Servo left;
    public static Servo right;
    public static double left_position = mid[0];
    public static double right_position = mid[1];

    private static final double SERVO_STEP = 0.005;

    public Differential(HardwareMap hardwareMap) {
        left = hardwareMap.get(Servo.class, "intakeLeft");
        right = hardwareMap.get(Servo.class, "intakeRight");
    }

    public static void setDifferential() {
        left.setPosition(left_position);
        right.setPosition(right_position);
    }

    public static void deposit() {
        left_position = up[0];
        right_position = up[1];
    }

    public static void intake() {
        left_position = down[0];
        right_position = down[1];
    }

    public static void setPosition(String position) {
        switch (position.toLowerCase()) {
            case "front":
                left_position = up[0];
                right_position = up[1];
                break;
            case "mid":
                left_position = mid[0];
                right_position = mid[1];
                break;
            case "back":
                left_position = down[0];
                right_position = down[1];
                break;
        }
    }

    public static void spinRight() {
        left_position = Math.min(1.0, left_position + SERVO_STEP);
        right_position = Math.min(1.0, right_position + SERVO_STEP);
    }

    public static void spinLeft() {
        left_position = Math.max(0.0, left_position - SERVO_STEP);
        right_position = Math.max(0.0, right_position - SERVO_STEP);
    }
}