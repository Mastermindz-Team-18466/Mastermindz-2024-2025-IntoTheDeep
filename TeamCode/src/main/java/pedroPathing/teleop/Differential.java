package pedroPathing.teleop;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Differential {
    static final double[] intake = new double[]{0.85, 0.15};
    static final double[] deposit = new double[]{0.5, 0.5};
    static final double[] specimanDeposit = new double[]{0.4, 0.6};
    static final double[] specimanDepositDown = new double[]{0.15, 0.85};
    static final double[] mid = new double[]{0.875, 0.675};
    static final double[] close = new double[]{1, 0};
    static final double[] autoIntake = new double[]{0.7, 0.3};

    public static Servo left;
    public static Servo right;
    public static double left_position = 0;
    public static double right_position = 0;
    private static final double SERVO_STEP = 0.05;

    public static double left_offset = 0;
    public static double right_offset = 0;

    public Differential(HardwareMap hardwareMap) {
        left = hardwareMap.get(Servo.class, "intakeLeft");
        right = hardwareMap.get(Servo.class, "intakeRight");

        left_position = left.getPosition();
        right_position = right.getPosition();
    }

    public static void resetOffsets() {
        left_offset = 0;
        right_offset = 0;
    }

    public static void setDifferential() {
        left.setPosition(left_position + left_offset);
        right.setPosition(right_position + right_offset);
    }

    public static void deposit() {
        left_position = deposit[0];
        right_position = deposit[1];
    }

    public static void close() {
        left_position = close[0];
        right_position = close[1];
    }
    public static void autoIntake() {
        left_position = autoIntake[0];
        right_position = autoIntake[1];
    }

    public static void mid() {
        left_position = mid[0];
        right_position = mid[1];
    }

    public static void intake() {
        left_position = intake[0];
        right_position = intake[1];
    }

    public static void specimanDeposit() {
        left_position = specimanDeposit[0];
        right_position = specimanDeposit[1];
    }

    public static void specimanDown() {
        left_position = specimanDepositDown[0];
        right_position = specimanDepositDown[1];
    }

    public static void setPosition(double left, double right) {
        left_position = left;
        right_position = right;
    }
}