package org.firstinspires.ftc.teamcode.testing;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.teleop.Differential;

@TeleOp
@Config
public class IntakeTest extends OpMode {
    private Differential differential;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        differential = new Differential(hardwareMap);
        Differential.setDifferential();
    }

    @Override
    public void loop() {
        if (gamepad1.a) {
            Differential.deposit();
            Differential.setDifferential();
        } else if (gamepad1.cross) {
            Differential.intake();
            Differential.setDifferential();
        } else if (gamepad1.dpad_left) {
            Differential.spinLeft();
            Differential.setDifferential();
        } else if (gamepad1.dpad_right) {
            Differential.spinRight();
            Differential.setDifferential();
        } else if (gamepad1.dpad_up) {
            Differential.setPosition("front");
            Differential.setDifferential();
        } else if (gamepad1.dpad_down) {
            Differential.setPosition("back");
            Differential.setDifferential();
        } else if (gamepad1.triangle) {
            Differential.setPosition("mid");
            Differential.setDifferential();
        }

        telemetry.addData("Left Servo Position", Differential.left.getPosition());
        telemetry.addData("Right Servo Position", Differential.right.getPosition());
        telemetry.update();
    }
}
