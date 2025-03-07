package org.firstinspires.ftc.teamcode.roadrunner.drive.brinopmodes;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.roadrunner.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.subsystem.LiftSubsystem;
import org.firstinspires.ftc.teamcode.subsystem.SlideSubsystem;
import org.firstinspires.ftc.teamcode.subsystem.ClawSubsystem;
import org.firstinspires.ftc.teamcode.util.Junction;


//TODO Improvements: make slide not jerk as much with delayed full extension and make the conestack timer increase linearly instead of being constant
//TODO make the slide extend mostly out in preload to save half a second
@Config
@Disabled
@Autonomous(name = "right side")
public class NewRightAuto extends LinearOpMode {

    public static double initial_x_pos = 53.8;//55.56;
    public static double initial_y_pos = 1.25;//-2;
    public static double initial_turn_angle = 237;
    public static double spline_x_pos = 50.5;//51;
    public static double spline_y_pos = -7.16;//5.5;
    public static double retrieve_y_pos = -26.33;//27.33;
    public static double deposit_x_pos = 53.33;//54.75;
    public static double deposit_y_pos = 1.2;//-3.33;
    public static double x_change = -0.4;
    public static double y_change = -0.1;

    private MotorEx dr4bLeftMotor, dr4bRightMotor;
    private SimpleServo claw, slide;
    private TouchSensor limitSwitch;

    private SampleMecanumDrive drive;
    private LiftSubsystem liftSub;
    private ClawSubsystem clawSub;
    private SlideSubsystem slideSub;

    int pickupPosition = -100;
    int coneCounter = 5;

    private enum DRIVE_PHASE {
        WAIT_FOR_PRELOAD,
        SLIDE,
        PRELOAD,
        DEPOSIT,
        WAIT_FOR_DEPOSIT,
        MOVE_TO_RETRIEVE,
        WAIT,
        RETRIEVE,
        WAIT_FOR_GRAB,
        PARK,
        IDLE
    }
    boolean delayedExtend = false;
    boolean delayedLift = false;
    ElapsedTime liftTimer = new ElapsedTime();
    ElapsedTime delayTimer = new ElapsedTime();
    ElapsedTime wait100 = new ElapsedTime();
    ElapsedTime wait250 = new ElapsedTime();
    ElapsedTime wait750 = new ElapsedTime();

    DRIVE_PHASE currentState = DRIVE_PHASE.IDLE;
    Pose2d startPose = new Pose2d(0, 0, Math.toRadians(180));

    @Override
    public void runOpMode() throws InterruptedException {
        double storedSplineX = spline_x_pos;
        double storedDepositX = deposit_x_pos;
        double storedDepositY = deposit_y_pos;

        dr4bLeftMotor = new MotorEx(hardwareMap, "dr4bLeft");
        dr4bRightMotor = new MotorEx(hardwareMap, "dr4bRight");
        dr4bLeftMotor.resetEncoder();
        dr4bRightMotor.resetEncoder();

        claw = new SimpleServo(hardwareMap, "claw", 0, 120);
        slide = new SimpleServo(hardwareMap, "slide", 0, 120);

        limitSwitch = hardwareMap.get(TouchSensor.class, "touch");

        drive = new SampleMecanumDrive(hardwareMap);
        slideSub = new SlideSubsystem(slide);
        liftSub = new LiftSubsystem(dr4bLeftMotor,dr4bRightMotor,limitSwitch);
        clawSub = new ClawSubsystem(claw);

        drive.setPoseEstimate(startPose);

        clawSub.grab();
        liftSub.setJunction(-75);
        delayTimer.reset();
        while(delayTimer.seconds()<=1){
            liftSub.updatePID();
        }

        telemetry.addData("initialized", true);
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;


        currentState = DRIVE_PHASE.WAIT_FOR_PRELOAD;

        while (opModeIsActive() && !isStopRequested()) {
            switch (currentState) {
                case WAIT_FOR_PRELOAD:
                    liftSub.setJunction(Junction.MEDIUM);
                    liftTimer.reset();
                    delayedLift = true;
                    drive.followTrajectoryAsync(drive.trajectoryBuilder(startPose)
                            .lineToLinearHeading(new Pose2d(initial_x_pos,initial_y_pos, Math.toRadians(initial_turn_angle))//, SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                            )//SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                            .build());
                    slideSub.setPos(0.42);
                    currentState = DRIVE_PHASE.SLIDE;
                    break;
                case SLIDE:
                    if(!drive.isBusy()){
                        drive.followTrajectoryAsync(drive.trajectoryBuilder(drive.getPoseEstimate())
                                .lineToLinearHeading(new Pose2d(initial_x_pos,initial_y_pos, Math.toRadians(initial_turn_angle))//, SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                )//SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                                .build());
                        slideSub.out();
                        wait750.reset();
                        currentState = DRIVE_PHASE.DEPOSIT;
                    }
                    break;
                case DEPOSIT:
                    if (!drive.isBusy() && wait750.seconds()>=0.3) {
                        currentState = DRIVE_PHASE.WAIT_FOR_DEPOSIT;
                        wait250.reset();
                    }
                    break;

                case WAIT_FOR_DEPOSIT:
                    if (wait250.seconds() >= 0) {
                        clawSub.release();
                        wait100.reset();
                        currentState = DRIVE_PHASE.MOVE_TO_RETRIEVE;
                    }
                    break;
                case MOVE_TO_RETRIEVE:
                    if(wait100.seconds() >= 0.1){
                        slideSub.in();
                        liftSub.setJunction(pickupPosition);
                        pickupPosition+=33;
                        if(coneCounter <= 0){
                            currentState = DRIVE_PHASE.PARK;
                            drive.followTrajectorySequenceAsync(drive.trajectorySequenceBuilder(drive.getPoseEstimate())
                                    .turn(Math.toRadians(-45))
                                    .build());
                        }else{
                            currentState = DRIVE_PHASE.WAIT;
                            drive.followTrajectorySequenceAsync(drive.trajectorySequenceBuilder(drive.getPoseEstimate())
                                    .splineTo(new Vector2d(spline_x_pos, spline_y_pos), Math.toRadians(-90), SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                            SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                                    .forward(Math.abs(retrieve_y_pos-spline_y_pos), SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                            SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                                    .build());
                            spline_x_pos += x_change;
                            retrieve_y_pos -= y_change;
                        }
                        coneCounter--;

                    }
                    break;

                case WAIT:
                    if(!drive.isBusy()){
                        liftTimer.reset();
                        currentState=DRIVE_PHASE.RETRIEVE;
                    }
                    break;
                case RETRIEVE:
                    if (liftTimer.seconds()>=0.3) {
                        clawSub.grab();
                        liftSub.setJunction(Junction.MEDIUM);
                        liftTimer.reset();
                        delayedLift = true;
                        wait750.reset();
                        currentState = DRIVE_PHASE.WAIT_FOR_GRAB;
                    }
                    break;
                case WAIT_FOR_GRAB:
                    if(wait750.seconds()>=0.5){
                        delayedExtend = true;
                        delayTimer.reset();
                        currentState = DRIVE_PHASE.DEPOSIT;
                        drive.followTrajectorySequenceAsync(drive.trajectorySequenceBuilder(drive.getPoseEstimate())
//                                .back(Math.abs(5),SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
//                                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                                .back(2//,SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                )//SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
//                                .splineTo(new Vector2d(deposit_x_pos, deposit_y_pos), Math.toRadians(312.64)//,SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
//                                )//SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                                .splineTo(new Vector2d(deposit_x_pos-Math.sin(Math.toRadians(57)*7)-3, deposit_y_pos-Math.cos(Math.toRadians(57))*7), Math.toRadians(57)//,SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                )//SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL))
                                .lineToLinearHeading(new Pose2d(deposit_x_pos, deposit_y_pos,Math.toRadians(237)))
                                .build());
                        deposit_x_pos += x_change;
                        deposit_y_pos -= y_change;
                    }
                    break;
                case PARK:
                    if(!drive.isBusy()){
                        currentState = DRIVE_PHASE.IDLE;
                    }
                    break;
                case IDLE:
                    deposit_y_pos = storedDepositY;
                    deposit_x_pos = storedDepositX;
                    spline_x_pos = storedSplineX;
                    break;
            }
            if(delayedExtend && delayTimer.seconds()>=0.2){
                delayedExtend = false;
                slideSub.out();
            }

            if(delayedLift && liftTimer.seconds()>=0.75){
                liftSub.setJunction(Junction.HIGH);
                delayedLift = false;
            }

            drive.update();
            liftSub.updatePID();

            Pose2d poseEstimate = drive.getPoseEstimate();
            telemetry.addData("x", poseEstimate.getX());
            telemetry.addData("y", poseEstimate.getY());
            telemetry.addData("heading", poseEstimate.getHeading());
            telemetry.addData("drive phase", currentState);
            telemetry.update();
        }
    }
}