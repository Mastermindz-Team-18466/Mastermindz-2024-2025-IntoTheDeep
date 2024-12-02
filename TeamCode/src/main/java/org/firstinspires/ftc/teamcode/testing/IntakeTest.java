package org.firstinspires.ftc.teamcode.testing;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp
@Config
public class IntakeTest extends OpMode {
    private Servo intakeLeft;
    private Servo intakeRight;

    public static double intakeLeftPosition = 0.5;
    public static double intakeRightPosition = 0.5;

    Gamepad currentGamepad1 = new Gamepad();
    Gamepad previousGamepad1 = new Gamepad();

    public boolean together = true;
    public double leftPosition = 0.5; // Start at midpoint
    public double rightPosition = 0.5; // Start at midpoint

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        intakeLeft = hardwareMap.get(Servo.class, "intakeLeft");
        intakeRight = hardwareMap.get(Servo.class, "intakeRight");

        intakeLeft.setPosition(intakeLeftPosition);
        intakeRight.setPosition(intakeRightPosition);
    }

    @Override
    public void loop() {
        previousGamepad1.copy(currentGamepad1);
        currentGamepad1.copy(gamepad1);

        // Proposed positions based on input
        double newLeftPosition = leftPosition;
        double newRightPosition = rightPosition;

        // Adjust positions based on D-Pad input
        if (gamepad1.dpad_down && !previousGamepad1.dpad_down) {
            newLeftPosition += 0.05;
            newRightPosition += 0.05;
        }
        if (gamepad1.dpad_up && !previousGamepad1.dpad_up) {
            newLeftPosition -= 0.05;
            newRightPosition -= 0.05;
        }
        if (gamepad1.dpad_left && !previousGamepad1.dpad_left) {
            newLeftPosition += 0.05;
            newRightPosition -= 0.05;
        }
        if (gamepad1.dpad_right && !previousGamepad1.dpad_right) {
            newLeftPosition -= 0.05;
            newRightPosition += 0.05;
        }

        // Check boundaries and prevent movement if one servo hits a limit
        boolean leftAtLimit = newLeftPosition < 0 || newLeftPosition > 1;
        boolean rightAtLimit = newRightPosition < 0 || newRightPosition > 1;

        if (!leftAtLimit && !rightAtLimit) {
            // Both servos are within bounds
            leftPosition = Math.max(0, Math.min(1, newLeftPosition));
            rightPosition = Math.max(0, Math.min(1, newRightPosition));
        } else {
            newLeftPosition = leftPosition;
            newRightPosition = rightPosition;
        }

        // Set servo positions
        intakeLeft.setPosition(leftPosition);
        intakeRight.setPosition(rightPosition);

        // Telemetry data
        telemetry.addData("Left Servo Position: ", intakeLeft.getPosition());
        telemetry.addData("Right Servo Position: ", intakeRight.getPosition());
        telemetry.addData("Left at Limit: ", leftAtLimit);
        telemetry.addData("Right at Limit: ", rightAtLimit);
        telemetry.update();
    }
}
