package org.firstinspires.ftc.teamcode.teleop;

import static org.firstinspires.ftc.teamcode.teleop.Claw.claw;

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
    Claw claw;
    Differential diffy;
    IntakeOuttake intakeOuttake;

    private Servo left;
    private Servo right;

    public static double forwardSpeed = 0.1;
    public static double reverseSpeed = -0.1;
    public static double stopSpeed = 0.0;

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        leftFront = hardwareMap.get(DcMotor.class, "FrontLeft");
        leftRear = hardwareMap.get(DcMotor.class, "BackLeft");
        rightRear = hardwareMap.get(DcMotor.class, "BackRight");
        rightFront = hardwareMap.get(DcMotor.class, "FrontRight");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        arm = new TelescopingArm(hardwareMap);
        claw = new Claw(hardwareMap);
        diffy = new Differential(hardwareMap);
        intakeOuttake = new IntakeOuttake(arm, claw, diffy);

        left = hardwareMap.get(Servo.class, "intakeLeft");
        right = hardwareMap.get(Servo.class, "intakeRight");

        Gamepad currentGamepad1 = new Gamepad();
        Gamepad currentGamepad2 = new Gamepad();

        Gamepad previousGamepad1 = new Gamepad();
        Gamepad previousGamepad2 = new Gamepad();

        intakeOuttake.setInstructions(IntakeOuttake.Instructions.CLOSED);
        intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.MAX_RETRACT);

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
            if (gamepad1.right_trigger > 0.5) {
                y = -gamepad1.left_stick_y * 1;
                x = gamepad1.left_stick_x * 1.1;
                rx = gamepad1.right_stick_x * 0.75;
            } else if (gamepad1.left_trigger > 0.5) {
                y = -gamepad1.left_stick_y * 0.3;
                x = gamepad1.left_stick_x * 0.3;
                rx = gamepad1.right_stick_x * 0.2;
            } else {
                y = -gamepad1.left_stick_y * 0.65;
                x = gamepad1.left_stick_x * 0.65;
                rx = gamepad1.right_stick_x * 0.5;
            }

            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            leftFront.setPower(frontLeftPower);
            leftRear.setPower(backLeftPower);
            rightFront.setPower(frontRightPower);
            rightRear.setPower(backRightPower);

            if (currentGamepad1.left_bumper && !previousGamepad1.left_bumper) {
                intakeOuttake.setInstructions(IntakeOuttake.Instructions.INTAKE);
                intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.INTAKE_EXTENSION);
            }

            if (currentGamepad1.triangle && !previousGamepad1.triangle) {
                intakeOuttake.setInstructions(IntakeOuttake.Instructions.DEPOSIT);
                intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.PITCH_DEPOSIT);
            }

            if (currentGamepad1.options && !previousGamepad1.options) {
                intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_DEPOSIT);
                intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.PITCH_DEPOSIT);
            }

            if (currentGamepad2.dpad_up && !previousGamepad2.dpad_up) {
                arm.extendTo(arm.extensionLeft.getCurrentPosition() + 100);
            }

            if (currentGamepad2.dpad_down && !previousGamepad2.dpad_down) {
                arm.extendTo(arm.extensionLeft.getCurrentPosition() - 100);
            }

            if (currentGamepad2.dpad_left && !previousGamepad2.dpad_left) {
                arm.pitchTo(arm.pitch.getCurrentPosition() - 100);
            }

            if (currentGamepad2.dpad_right && !previousGamepad2.dpad_right) {
                arm.pitchTo(arm.pitch.getCurrentPosition() + 100);
            }

            if (currentGamepad2.triangle && !previousGamepad2.triangle) {
                diffy.setPosition(diffy.left.getPosition() - 0.05, diffy.right.getPosition() + 0.05);
            }

            if (currentGamepad2.triangle && !previousGamepad2.triangle) {
                diffy.setPosition(diffy.left.getPosition() - 0.05, diffy.right.getPosition() + 0.05);
            }

            if (currentGamepad2.cross && !previousGamepad2.cross) {
                diffy.setPosition(diffy.left.getPosition() + 0.05, diffy.right.getPosition() - 0.05);
            }

            if (currentGamepad2.square && !previousGamepad2.square) {
                diffy.setPosition(diffy.left.getPosition() - 0.05, diffy.right.getPosition() - 0.05);
            }

            if (currentGamepad2.circle && !previousGamepad2.circle) {
                diffy.setPosition(diffy.left.getPosition() + 0.05, diffy.right.getPosition() + 0.05);
            }

            if (currentGamepad1.square && !previousGamepad1.square) {
                intakeOuttake.setInstructions(IntakeOuttake.Instructions.CLOSE_CLAW);
                intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.CLOSE_CLAW);
            }

            if (currentGamepad1.circle && !previousGamepad1.circle) {
                intakeOuttake.setInstructions(IntakeOuttake.Instructions.OPEN_CLAW);
                intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.OPEN_CLAW);
            }

            if (currentGamepad1.share && !previousGamepad1.share) {
                intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_DEPOSIT_DOWN);
                intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.SPECIMAN_EXTEND);
            }

            if (currentGamepad1.right_bumper && !previousGamepad1.right_bumper) {
                if (intakeOuttake.arm.pitch.getCurrentPosition() < 1800 || intakeOuttake.claw.claw.getPosition() == 0) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.HOLD);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.MAX_RETRACT);
                }
            }

            intakeOuttake.update();

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("FrontLeft Power", frontLeftPower);
            telemetry.addData("BackLeft Power", backLeftPower);
            telemetry.addData("FrontRight Power", frontRightPower);
            telemetry.addData("BackRight Power", backRightPower);
            telemetry.addData("LEFT", diffy.left.getPosition());
            telemetry.addData("RIGHT", diffy.right.getPosition());
            telemetry.update();
        }
    }
}
