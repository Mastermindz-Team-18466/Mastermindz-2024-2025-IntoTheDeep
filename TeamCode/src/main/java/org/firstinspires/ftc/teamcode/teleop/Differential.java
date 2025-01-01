package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Differential {
    private static final double[] up = new double[]{0.85, 0.15};
    private static final double[] down = new double[]{0.25, 0.75};
    public static Servo left;
    public static Servo right;
    public static double left_position = 1;
    public static double right_position = 0;

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
}
