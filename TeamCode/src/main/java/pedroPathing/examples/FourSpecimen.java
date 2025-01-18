package pedroPathing.examples;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.localization.PoseUpdater;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.Point;
import com.pedropathing.util.Constants;
import com.pedropathing.util.DashboardPoseTracker;
import com.pedropathing.util.Drawing;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import  com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Servo;

import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;
import pedroPathing.teleop.Claw;
import pedroPathing.teleop.Differential;
import pedroPathing.teleop.IntakeOuttake;
import pedroPathing.teleop.TelescopingArm;

/**
 * This is an example auto that showcases movement and control of two servos autonomously.
 * It is a 0+4 (Specimen + Sample) bucket auto. It scores a neutral preload and then pickups 3 samples from the ground and scores them before parking.
 * There are examples of different ways to build paths.
 * A path progression method has been created and can advance based on time, position, or other factors.
 *
 * @author Baron Henderson - 20077 The Indubitables
 * @version 2.0, 11/28/2024
 */

@Config
@Autonomous(name = "Four Specimen", group = "Examples")
public class FourSpecimen extends OpMode {
    private PoseUpdater poseUpdater;
    private DashboardPoseTracker dashboardPoseTracker;
    private IntakeOuttake intakeOuttake;
    private Follower follower;
    private Timer pathTimer, actionTimer, opmodeTimer;
    private TelescopingArm arm;
    private Differential diffy;
    private Claw claw;
    private Servo latch;

    /** This is the variable where we store the state of our auto.
     * It is used by the pathUpdate method. */
    private int pathState;

    /* Create and Define Poses + Paths
     * Poses are built with three constructors: x, y, and heading (in Radians).
     * Pedro uses 0 - 144 for x and y, with 0, 0 being on the bottom left.
     * (For Into the Deep, this would be Blue Observation Zone (0,0) to Red Observation Zone (144,144).)
     * Even though Pedro uses a different coordinate system than RR, you can convert any roadrunner pose by adding +72 both the x and y.
     * This visualizer is very easy to use to find and create paths/pathchains/poses: <https://pedro-path-generator.vercel.app/>
     * Lets assume our robot is 18 by 18 inches
     * Lets assume the Robot is facing the human player and we want to score in the bucket */

    private final Pose startPose = new Pose(0, 0, Math.toRadians(0));
    private final Pose midScorePose = new Pose(5.2, 0, Math.toRadians(0));
    private final Pose scorePose = new Pose(19.5, 0, Math.toRadians(0));
    private final Pose intakePose1 = new Pose(11, -8, Math.toRadians(-30));
    private final Pose specimenPose1 = new Pose(14.5, -11, Math.toRadians(-62.5));
    private final Pose specimenPose1Deposit = new Pose(14.48, -12.3, Math.toRadians(-133.6));
    private final Pose specimenPose2 = new Pose(16, -17.3, Math.toRadians(-63.2));
    private final Pose specimenPose2Deposit = new Pose(13.05, -12.82, Math.toRadians(-137.7));
    private final Pose pickUp = new Pose(7.1, -21.5, Math.toRadians(-180));
    private final Pose scorePose1 = new Pose(15.5, 5, Math.toRadians(-180));
    private final Pose backPose = new Pose(19.5, 5, Math.toRadians(-180));
    private final Pose forwardPose = new Pose(17, 5, Math.toRadians(-180));

    private final Pose scorePose2 = new Pose(15.5, 7, Math.toRadians(-180));
    private final Pose backPose2 = new Pose(19, 7, Math.toRadians(-180));
    private final Pose forwardPose2 = new Pose(17, 7, Math.toRadians(-180));

    private final Pose scorePose3 = new Pose(15.5, 0, Math.toRadians(-180));
    private final Pose backPose3 = new Pose(19, 0, Math.toRadians(-180));
    private final Pose forwardPose3 = new Pose(17, 0, Math.toRadians(-180));
    /* These are our Paths and PathChains that we will define in buildPaths() */
    private Path back1, midScorePreload, scorePreload, intakePath1, specimenPath1, specimenPath1Deposit, specimenPath2, specimenPath2Deposit, pickUpFromDeposit, deposit1, forward1, back2, forward2, deposit2, back3, forward3, deposit3;

    /** Build the paths for the auto (adds, for example, constant/linear headings while doing paths)
     * It is necessary to do this so that all the paths are built before the auto starts. **/
    public void buildPaths() {

        /* There are two major types of paths components: BezierCurves and BezierLines.
         *    * BezierCurves are curved, and require >= 3 points. There are the start and end points, and the control points.
         *    - Control points manipulate the curve between the start and end points.
         *    - A good visualizer for this is [this](https://pedro-path-generator.vercel.app/).
         *    * BezierLines are straight, and require 2 points. There are the start and end points.
         * Paths have can have heading interpolation: Constant, Linear, or Tangential
         *    * Linear heading interpolation:
         *    - Pedro will slowly change the heading of the robot from the startHeading to the endHeading over the course of the entire path.
         *    * Constant Heading Interpolation:
         *    - Pedro will maintain one heading throughout the entire path.
         *    * Tangential Heading Interpolation:
         *    - Pedro will follows the angle of the path such that the robot is always driving forward when it follows the path.
         * PathChains hold Path(s) within it and are able to hold their end point, meaning that they will holdPoint until another path is followed.
         * Here is a explanation of the difference between Paths and PathChains <https://pedropathing.com/commonissues/pathtopathchain.html> */

        /* This is our scorePreload path. We are using a BezierLine, which is a straight line. */
        midScorePreload = new Path(new BezierLine(new Point(startPose), new Point(midScorePose)));
        midScorePreload.setLinearHeadingInterpolation(startPose.getHeading(), midScorePose.getHeading());

        scorePreload = new Path(new BezierLine(new Point(midScorePose), new Point(scorePose)));
        scorePreload.setLinearHeadingInterpolation(midScorePose.getHeading(), scorePose.getHeading());

        intakePath1 = new Path(new BezierLine(new Point(scorePose), new Point(intakePose1)));
        intakePath1.setLinearHeadingInterpolation(scorePose.getHeading(), intakePose1.getHeading());

        specimenPath1 = new Path(new BezierLine(new Point(intakePose1), new Point(specimenPose1)));
        specimenPath1.setLinearHeadingInterpolation(intakePose1.getHeading(), specimenPose1.getHeading());

        specimenPath1Deposit = new Path(new BezierLine(new Point(specimenPose1), new Point(specimenPose1Deposit)));
        specimenPath1Deposit.setConstantHeadingInterpolation(specimenPose1Deposit.getHeading());

        specimenPath2 = new Path(new BezierLine(new Point(specimenPose1Deposit), new Point(specimenPose2)));
        specimenPath2.setLinearHeadingInterpolation(specimenPose1Deposit.getHeading(), specimenPose2.getHeading());

        specimenPath2Deposit = new Path(new BezierLine(new Point(specimenPose2), new Point(specimenPose2Deposit)));
        specimenPath2Deposit.setLinearHeadingInterpolation(specimenPose2.getHeading(), specimenPose2Deposit.getHeading());

        pickUpFromDeposit = new Path(new BezierLine(new Point(specimenPose2Deposit), new Point(pickUp)));
        pickUpFromDeposit.setLinearHeadingInterpolation(specimenPose2Deposit.getHeading(), pickUp.getHeading());

        deposit1 = new Path(new BezierLine(new Point(pickUp), new Point(scorePose1)));
        deposit1.setConstantHeadingInterpolation(scorePose1.getHeading());

        back1 = new Path(new BezierLine(new Point(scorePose), new Point(backPose)));
        back1.setConstantHeadingInterpolation(backPose.getHeading());

        forward1 = new Path(new BezierLine(new Point(backPose), new Point(forwardPose)));
        forward1.setConstantHeadingInterpolation(forwardPose.getHeading());

        deposit2 = new Path(new BezierLine(new Point(pickUp), new Point(scorePose2)));
        deposit2.setConstantHeadingInterpolation(scorePose2.getHeading());

        back2 = new Path(new BezierLine(new Point(scorePose2), new Point(backPose2)));
        back2.setConstantHeadingInterpolation(backPose2.getHeading());

        forward2 = new Path(new BezierLine(new Point(backPose2), new Point(forwardPose2)));
        forward2.setConstantHeadingInterpolation(forwardPose2.getHeading());

        deposit3 = new Path(new BezierLine(new Point(pickUp), new Point(scorePose3)));
        deposit3.setConstantHeadingInterpolation(scorePose3.getHeading());

        back3 = new Path(new BezierLine(new Point(scorePose3), new Point(backPose3)));
        back3.setConstantHeadingInterpolation(backPose3.getHeading());

        forward3 = new Path(new BezierLine(new Point(backPose3), new Point(forwardPose3)));
        forward3.setConstantHeadingInterpolation(forwardPose3.getHeading());
    }

    /** This switch is called continuously and runs the pathing, at certain points, it triggers the action state.
     * Everytime the switch changes case, it will reset the timer. (This is because of the setPathState() method)
     * The followPath() function sets the follower to run the specific path, but does NOT wait for it to finish before moving on. */
    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                intakeOuttake.closed_zero_out = false;
                intakeOuttake.arm.override_pitch = true;
                intakeOuttake.setInstructions(IntakeOuttake.Instructions.FRONT_SPECIMAN_DEPOSIT);
                intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.PITCH_DEPOSIT);
                follower.followPath(midScorePreload, true);
                setPathState(1);
                break;
            case 1:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (midScorePose.getX() - 2) && follower.getPose().getY() > (midScorePose.getY() - 2)) {
                    follower.followPath(scorePreload, true);
                    setPathState(2);
                }
                break;
            case 2:
                if(pathTimer.getElapsedTimeSeconds() > 1.5 && follower.getPose().getX() > (scorePose.getX() - 2) && follower.getPose().getY() > (scorePose.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.OPEN_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.OPEN_CLAW);
                    setPathState(3);
                }
                break;
            case 3:
                if(pathTimer.getElapsedTimeSeconds() > 0.75 && follower.getPose().getX() > (scorePose.getX() - 2) && follower.getPose().getY() > (scorePose.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.HOLD);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.MAX_RETRACT);
                    follower.followPath(intakePath1, true);
                    setPathState(4);
                }
                break;
            case 4:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (intakePose1.getX() - 2) && follower.getPose().getY() > (intakePose1.getY() - 2)) {
                    follower.followPath(specimenPath1, true);
                    setPathState(5);
                }
                break;
            case 5:
                if(pathTimer.getElapsedTimeSeconds() > 1 && follower.getPose().getX() > (specimenPose1.getX() - 2) && follower.getPose().getY() > (specimenPose1.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.EXTEND_TO_ONE_SPEC);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.INTAKE_EXTENSION);
                    setPathState(6);
                }
                break;
            case 6:
                if(pathTimer.getElapsedTimeSeconds() > 1 && follower.getPose().getX() > (specimenPose1.getX() - 2) && follower.getPose().getY() > (specimenPose1.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.CLOSE_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.CLOSE_CLAW);
                    setPathState(7);
                }
                break;
            case 7:
                if(pathTimer.getElapsedTimeSeconds() > 1 && follower.getPose().getX() > (specimenPose1.getX() - 2) && follower.getPose().getY() > (specimenPose1.getY() - 2)) {
                    follower.followPath(specimenPath1Deposit, true);
                    setPathState(9);
                }
                break;
            case 9:
                if(pathTimer.getElapsedTimeSeconds() > 0.75 && follower.getPose().getX() > (specimenPose1Deposit.getX() - 2) && follower.getPose().getY() > (specimenPose1Deposit.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPEC_OPEN_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.OPEN_CLAW);
                    setPathState(10);
                }
                break;
            case 10:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (specimenPose1Deposit.getX() - 2) && follower.getPose().getY() > (specimenPose1Deposit.getY() - 2)) {
                    follower.followPath(specimenPath2, true);
                    setPathState(11);
                }
                break;
            case 11:
                if(pathTimer.getElapsedTimeSeconds() > 1 && follower.getPose().getX() > (specimenPose2.getX() - 2) && follower.getPose().getY() > (specimenPose2.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.EXTEND_TO_TWO_SPEC);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.INTAKE_EXTENSION);
                    setPathState(12);
                }
                break;
            case 12:
                if(pathTimer.getElapsedTimeSeconds() > 1 && follower.getPose().getX() > (specimenPose2.getX() - 2) && follower.getPose().getY() > (specimenPose2.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.CLOSE_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.CLOSE_CLAW);
                    setPathState(13);
                }
                break;
            case 13:
                if(pathTimer.getElapsedTimeSeconds() > 1 && follower.getPose().getX() > (specimenPose2.getX() - 2) && follower.getPose().getY() > (specimenPose2.getY() - 2)) {
                    follower.followPath(specimenPath2Deposit, true);
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.EXTEND_TO_DROP);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.INTAKE_EXTENSION);
                    setPathState(14);
                }
                break;
            case 14:
                if(pathTimer.getElapsedTimeSeconds() > 0.75 && follower.getPose().getX() > (specimenPose2Deposit.getX() - 2) && follower.getPose().getY() > (specimenPose2Deposit.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPEC_OPEN_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.OPEN_CLAW);
                    setPathState(15);
                }
                break;
            case 15:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (specimenPose2Deposit.getX() - 2) && follower.getPose().getY() > (specimenPose2Deposit.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_INTAKE);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.INTAKE_EXTENSION);
                    setPathState(16);
                }
                break;
            case 16:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (specimenPose2Deposit.getX() - 2) && follower.getPose().getY() > (specimenPose2Deposit.getY() - 2)) {
                    follower.followPath(pickUpFromDeposit);
                    setPathState(17);
                }
                break;
            case 17:
                if(pathTimer.getElapsedTimeSeconds() > 1 && follower.getPose().getX() > (pickUp.getX() - 2) && follower.getPose().getY() > (pickUp.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.CLOSE_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.CLOSE_CLAW);
                    setPathState(18);
                }
                break;
            case 18:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (pickUp.getX() - 2) && follower.getPose().getY() > (pickUp.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_DEPOSIT);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.PITCH_DEPOSIT);
                    follower.followPath(deposit1, true);
                    setPathState(19);
                }
                break;
            case 19:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (scorePose1.getX() - 2) && follower.getPose().getY() > (scorePose1.getY() - 2)) {
                    follower.followPath(back1, true);
                    setPathState(20);
                }
                break;
            case 20:
                if(pathTimer.getElapsedTimeSeconds() > 0.75 && follower.getPose().getX() > (scorePose1.getX() - 2) && follower.getPose().getY() > (scorePose1.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_DEPOSIT_DOWN);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.SPECIMAN_EXTEND);
                    setPathState(21);
                }
                break;
            case 21:
                if(pathTimer.getElapsedTimeSeconds() > 0.5) {
                    follower.followPath(forward1, true);
                    setPathState(22);
                }
                break;
            case 22:
                if(pathTimer.getElapsedTimeSeconds() > 0.2 && follower.getPose().getX() > (forwardPose.getX() - 2) && follower.getPose().getY() > (forwardPose.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.OPEN_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.OPEN_CLAW);
                    setPathState(23);
                }
                break;
            case 23:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (specimenPose2Deposit.getX() - 2) && follower.getPose().getY() > (specimenPose2Deposit.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_INTAKE);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.INTAKE_EXTENSION);
                    setPathState(24);
                }
                break;
            case 24:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (specimenPose2Deposit.getX() - 2) && follower.getPose().getY() > (specimenPose2Deposit.getY() - 2)) {
                    follower.followPath(pickUpFromDeposit);
                    setPathState(25);
                }
                break;
            case 25:
                if(pathTimer.getElapsedTimeSeconds() > 1.5 && follower.getPose().getX() > (pickUp.getX() - 2) && follower.getPose().getY() > (pickUp.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.CLOSE_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.CLOSE_CLAW);
                    setPathState(26);
                }
                break;
            case 26:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (pickUp.getX() - 2) && follower.getPose().getY() > (pickUp.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_DEPOSIT);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.PITCH_DEPOSIT);
                    follower.followPath(deposit2, true);
                    setPathState(27);
                }
                break;
            case 27:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (scorePose2.getX() - 2) && follower.getPose().getY() > (scorePose2.getY() - 2)) {
                    follower.followPath(back2, true);
                    setPathState(28);
                }
                break;
            case 28:
                if(pathTimer.getElapsedTimeSeconds() > 0.75 && follower.getPose().getX() > (scorePose2.getX() - 2) && follower.getPose().getY() > (scorePose2.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_DEPOSIT_DOWN);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.SPECIMAN_EXTEND);
                    setPathState(29);
                }
                break;
            case 29:
                if(pathTimer.getElapsedTimeSeconds() > 0.5) {
                    follower.followPath(forward2, true);
                    setPathState(30);
                }
                break;
            case 30:
                if(pathTimer.getElapsedTimeSeconds() > 0.2 && follower.getPose().getX() > (forwardPose2.getX() - 2) && follower.getPose().getY() > (forwardPose2.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.OPEN_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.OPEN_CLAW);
                    setPathState(31);
                }
                break;
            case 31:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (specimenPose2Deposit.getX() - 2) && follower.getPose().getY() > (specimenPose2Deposit.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_INTAKE);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.INTAKE_EXTENSION);
                    setPathState(32);
                }
                break;
            case 32:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (specimenPose2Deposit.getX() - 2) && follower.getPose().getY() > (specimenPose2Deposit.getY() - 2)) {
                    follower.followPath(pickUpFromDeposit);
                    setPathState(33);
                }
                break;
            case 33:
                if(pathTimer.getElapsedTimeSeconds() > 1.5 && follower.getPose().getX() > (pickUp.getX() - 2) && follower.getPose().getY() > (pickUp.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.CLOSE_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.CLOSE_CLAW);
                    setPathState(34);
                }
                break;
            case 34:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (pickUp.getX() - 2) && follower.getPose().getY() > (pickUp.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_DEPOSIT);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.PITCH_DEPOSIT);
                    follower.followPath(deposit3, true);
                    setPathState(35);
                }
                break;
            case 35:
                if(pathTimer.getElapsedTimeSeconds() > 0.5 && follower.getPose().getX() > (scorePose3.getX() - 2) && follower.getPose().getY() > (scorePose3.getY() - 2)) {
                    follower.followPath(back3, true);
                    setPathState(36);
                }
                break;
            case 36:
                if(pathTimer.getElapsedTimeSeconds() > 0.75 && follower.getPose().getX() > (scorePose3.getX() - 2) && follower.getPose().getY() > (scorePose3.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.SPECIMAN_DEPOSIT_DOWN);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.SPECIMAN_EXTEND);
                    setPathState(37);
                }
                break;
            case 37:
                if(pathTimer.getElapsedTimeSeconds() > 0.5) {
                    follower.followPath(forward3, true);
                    setPathState(38);
                }
                break;
            case 38:
                if(pathTimer.getElapsedTimeSeconds() > 0.2 && follower.getPose().getX() > (forwardPose3.getX() - 2) && follower.getPose().getY() > (forwardPose3.getY() - 2)) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.OPEN_CLAW);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.OPEN_CLAW);
                    setPathState(39);
                }
                break;
            case 39:
                if(pathTimer.getElapsedTimeSeconds() > 0.1) {
                    intakeOuttake.setInstructions(IntakeOuttake.Instructions.HOLD);
                    intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.MAX_RETRACT);
                    setPathState(40);
                }
                break;


        }
    }

    /** These change the states of the paths and actions
     * It will also reset the timers of the individual switches **/
    public void setPathState(int pState) {
        pathState = pState;
        pathTimer.resetTimer();
    }

    /** This is the main loop of the OpMode, it will run repeatedly after clicking "Play". **/
    @Override
    public void loop() {
        poseUpdater.update();
        dashboardPoseTracker.update();

        // These loop the movements of the robot
        follower.update();
        intakeOuttake.update();
        autonomousPathUpdate();

        // Feedback to Driver Hub
        telemetry.addData("path state", pathState);
        telemetry.addData("x", follower.getPose().getX());
        telemetry.addData("y", follower.getPose().getY());
        telemetry.addData("heading", follower.getPose().getHeading());
        telemetry.addData("pitch", intakeOuttake.arm.pitch.getCurrentPosition());
        telemetry.addData("arm", intakeOuttake.arm.extensionLeft.getCurrentPosition());
        telemetry.update();

        Drawing.drawPoseHistory(dashboardPoseTracker, "#4CAF50");
        Drawing.drawRobot(poseUpdater.getPose(), "#4CAF50");
        Drawing.sendPacket();
    }

    /** This method is called once at the init of the OpMode. **/
    @Override
    public void init() {
        arm = new TelescopingArm(hardwareMap);
        claw = new Claw(hardwareMap);
        diffy = new Differential(hardwareMap);
        intakeOuttake = new IntakeOuttake(arm, claw, diffy);

        poseUpdater = new PoseUpdater(hardwareMap);
        dashboardPoseTracker = new DashboardPoseTracker(poseUpdater);

        poseUpdater.setStartingPose(new Pose(startPose.getX(), startPose.getY(), startPose.getHeading()));

        poseUpdater.update();
        dashboardPoseTracker.update();

        pathTimer = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();

        Drawing.drawRobot(poseUpdater.getPose(), "#4CAF50");
        Drawing.sendPacket();

        intakeOuttake.setInstructions(IntakeOuttake.Instructions.CLOSED);
        intakeOuttake.setSpecificInstruction(IntakeOuttake.SpecificInstructions.MAX_RETRACT);
        double startTime = System.currentTimeMillis();

        intakeOuttake.arm.pitch_zeroed = true;
        latch = hardwareMap.get(Servo.class, "latch");

        while (System.currentTimeMillis() - startTime <= 5000) { }

        latch.getController().pwmEnable();
        latch.setPosition(0);

        Constants.setConstants(FConstants.class, LConstants.class);
        follower = new Follower(hardwareMap);
        follower.setStartingPose(startPose);
        follower.startTeleopDrive();
        buildPaths();
    }

    /** This method is called continuously after Init while waiting for "play". **/
    @Override
    public void init_loop() {
        intakeOuttake.update();
    }

    /** This method is called once at the start of the OpMode.
     * It runs all the setup actions, including building paths and starting the path system **/
    @Override
    public void start() {
        opmodeTimer.resetTimer();

        latch.setPosition(1);

        intakeOuttake.closed_zero_out = false;
        intakeOuttake.arm.override_pitch = true;
        intakeOuttake.arm.resetOffsets();

        setPathState(0);
    }

    /** We do not use this because everything should automatically disable **/
    @Override
    public void stop() {
    }
}

