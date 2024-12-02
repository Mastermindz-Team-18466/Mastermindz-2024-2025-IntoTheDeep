package org.firstinspires.ftc.teamcode.teleop;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.drive.MecanumDrive;
import org.firstinspires.ftc.teamcode.drive.wolf.WolfDrive;

@TeleOp(name = "TeleOpMode", group = "Concept")
public class TeleOpMode extends LinearOpMode {
    private ElapsedTime runtime = new ElapsedTime();
    public double slowMode = 1;

    DcMotor leftFront;
    DcMotor leftRear;
    DcMotor rightRear;
    DcMotor rightFront;
    TelescopingArm arm;
    private Servo intakeLeft;
    private Servo intakeRight;
    public static double intakeLeftPosition = 0.5;
    public static double intakeRightPosition = 0.5;
    private DcMotorEx left;
    private DcMotorEx right;
    private Servo servo;

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        leftRear = hardwareMap.get(DcMotor.class, "leftBack");
        rightRear = hardwareMap.get(DcMotor.class, "rightBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        intakeLeft = hardwareMap.get(Servo.class, "intakeLeft");
        intakeRight = hardwareMap.get(Servo.class, "intakeRight");

        intakeLeft.setPosition(intakeLeftPosition);
        intakeRight.setPosition(intakeRightPosition);

        arm = new TelescopingArm(hardwareMap);

        left = hardwareMap.get(DcMotorEx.class, "firststring");
        right = hardwareMap.get(DcMotorEx.class, "secondstring");

        left.setDirection(DcMotorSimple.Direction.FORWARD);
        right.setDirection(DcMotorSimple.Direction.REVERSE);
        servo = hardwareMap.get(Servo.class, "servo");

        Gamepad currentGamepad1 = new Gamepad();
        Gamepad currentGamepad2 = new Gamepad();

        Gamepad previousGamepad1 = new Gamepad();
        Gamepad previousGamepad2 = new Gamepad();

        double verticalTargetPosition = 0;

        waitForStart();
        runtime.reset();

        while (opModeIsActive()) {
            previousGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);

            previousGamepad2.copy(currentGamepad2);
            currentGamepad2.copy(gamepad2);

            double y = 0;
            double x = 0;
            double rx = 0;
            if (gamepad1.right_trigger > 0.5){
                y = -gamepad1.left_stick_y * 1;
                x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
                rx = gamepad1.right_stick_x* 0.75;
            } else if (gamepad1.left_trigger > 0.5) {
                y = -gamepad1.left_stick_y* 0.3; // Remember, this is reversed!
                x = gamepad1.left_stick_x * 0.3; // Counteract imperfect strafing
                rx = gamepad1.right_stick_x* 0.2;
            } else {
                y = -gamepad1.left_stick_y * 0.65; // Remember, this is reversed!
                x = gamepad1.left_stick_x * 0.65; // Counteract imperfect strafing
                rx = gamepad1.right_stick_x * 0.5;
            }


            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio, but only when
            // at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            leftFront.setPower(frontLeftPower);
            leftRear.setPower(backLeftPower);
            rightFront.setPower(frontRightPower);
            rightRear.setPower(backRightPower);

            if (currentGamepad1.triangle && !previousGamepad1.triangle) {
                verticalTargetPosition = 1600;
                arm.pitchTo(verticalTargetPosition);
            }
            if (currentGamepad1.circle && !previousGamepad1.circle) {
                verticalTargetPosition = 1700;
                arm.pitchTo(verticalTargetPosition);
            }
            if (currentGamepad1.cross && !previousGamepad1.cross) {
                verticalTargetPosition = 500;
                arm.pitchTo(verticalTargetPosition);
            }
            if (currentGamepad1.square && !previousGamepad1.square) {
                verticalTargetPosition = 300;
                arm.pitchTo(verticalTargetPosition);
            }

            if (currentGamepad2.dpad_down && !previousGamepad2.dpad_down) {
                verticalTargetPosition -= 50;
                arm.pitchTo(verticalTargetPosition);
            }

            if (currentGamepad2.dpad_up && !previousGamepad2.dpad_up) {
                verticalTargetPosition += 50;
                arm.pitchTo(verticalTargetPosition);
            }

            arm.setPitch();

            double newLeftPosition = intakeLeftPosition;
            double newRightPosition = intakeRightPosition;

            // Adjust positions based on D-Pad input
            if (gamepad1.dpad_left && !previousGamepad1.dpad_left) {
                newLeftPosition += 0.02;
                newRightPosition += 0.02;
            }
            if (gamepad1.dpad_right && !previousGamepad1.dpad_right) {
                newLeftPosition -= 0.02;
                newRightPosition -= 0.02;
            }
            if (gamepad1.dpad_down && !previousGamepad1.dpad_down) {
                newLeftPosition += 0.02;
                newRightPosition -= 0.02;
            }
            if (gamepad1.dpad_up && !previousGamepad1.dpad_up) {
                newLeftPosition -= 0.02;
                newRightPosition += 0.02;
            }

            boolean leftAtLimit = newLeftPosition < 0 || newLeftPosition > 1;
            boolean rightAtLimit = newRightPosition < 0 || newRightPosition > 1;

            if (!leftAtLimit && !rightAtLimit) {
                // Both servos are within bounds
                intakeLeftPosition = Math.max(0, Math.min(1, newLeftPosition));
                intakeRightPosition = Math.max(0, Math.min(1, newRightPosition));
            } else {
                newLeftPosition = intakeLeftPosition;
                newRightPosition = intakeRightPosition;
            }

            // Set servo positions
            intakeLeft.setPosition(intakeLeftPosition);
            intakeRight.setPosition(intakeRightPosition);

            if (currentGamepad2.cross && !previousGamepad2.cross) {
                servo.setPosition(0.7);
            }
            if (currentGamepad2.circle && !previousGamepad2.circle){
                servo.setPosition(0.3);
            }

            left.setPower(gamepad2.left_stick_y);
            right.setPower(gamepad2.left_stick_y);

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("FrontLeft Power", frontLeftPower);
            telemetry.addData("BackLeft Power", backLeftPower);
            telemetry.addData("FrontRight Power", frontRightPower);
            telemetry.addData("BackRight Power", backRightPower);
            telemetry.addData("Left Intake: ", intakeLeft.getPosition());
            telemetry.addData("Right Intake: ", intakeRight.getPosition());
            telemetry.update();
        }
    }
}