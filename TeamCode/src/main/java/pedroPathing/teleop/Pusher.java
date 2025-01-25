package pedroPathing.teleop;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Pusher {
    private static final double open = 0.65;
    private static final double superOpen = 0.5;
    public static double close = 1;
    public static Servo pusher;
    public static double position = 1;
    public static boolean opened = false;

    public Pusher(HardwareMap hardwareMap) {
        pusher = hardwareMap.get(Servo.class, "pusher");
    }

    public static void setPusher() {
        pusher.setPosition(position);
    }

    public static void open() {
        position = open;
        opened = true;
    }

    public static void superOpen() {
        position = superOpen;
        opened = true;
    }

    public static void close() {
        position = close;
        opened = false;
    }
}
