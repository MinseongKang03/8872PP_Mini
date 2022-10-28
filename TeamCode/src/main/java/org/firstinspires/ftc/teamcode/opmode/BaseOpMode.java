package org.firstinspires.ftc.teamcode.opmode;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.hardware.RevIMU;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import org.firstinspires.ftc.teamcode.subsystem.ArmSubsystem;
import org.firstinspires.ftc.teamcode.subsystem.DriveSubsystem;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BaseOpMode extends CommandOpMode {
    protected MotorEx fL, fR, bL, bR, dr4bLeftMotor, dr4bRightMotor;
    protected SimpleServo claw, slide1, slide2;
    protected DriveSubsystem drive;
    protected ArmSubsystem arm;
    protected RevIMU imu;

    @Override
    public void initialize() {
        initHardware();

        drive = new DriveSubsystem(fL, fR, bL, bR);
        arm = new ArmSubsystem(claw, slide1, slide2, dr4bLeftMotor, dr4bRightMotor);
        imu = new RevIMU(hardwareMap);
        imu.init();

        composeTelemetry();
        telemetry.addData("Mode", "Done initializing");
        telemetry.update();
    }

    protected void initHardware() {
        fL = new MotorEx(hardwareMap, "leftFront");
        fR = new MotorEx(hardwareMap, "rightFront");
        bL = new MotorEx(hardwareMap, "leftBack");
        bR = new MotorEx(hardwareMap, "rightBack");
        dr4bLeftMotor = new MotorEx(hardwareMap, "dr4bLeft");
        dr4bRightMotor = new MotorEx(hardwareMap, "dr4bRight");
        // what the proper min and max?
        claw = new SimpleServo(hardwareMap, "claw", 0, 120);
        slide1 = new SimpleServo(hardwareMap, "slide1", 0, 120);
        slide2 = new SimpleServo(hardwareMap, "slide2", 0, 120);

    }

    protected void composeTelemetry() {
        telemetry.addData("leftFront Power", () -> round(fL.motor.getPower()));
        telemetry.addData("leftBack Power", () -> round(bL.motor.getPower()));
        telemetry.addData("rightFront Power", () -> round(fR.motor.getPower()));
        telemetry.addData("rightBack Power", () -> round(bR.motor.getPower()));
        telemetry.addData("dr4bLeftMotor Power", () -> round(dr4bLeftMotor.motor.getPower()));
        telemetry.addData("dr4bRightMotor Power", () -> round(dr4bRightMotor.motor.getPower()));

        telemetry.addData("claw Position", () -> claw.getPosition());
        telemetry.addData("slide1 Position", () -> claw.getPosition());

        telemetry.addData("IMU Heading", () -> imu.getHeading());
    }

    protected void setUpHardwareDevices() {
        // reverse motors
    }


    private static double round(double value, @SuppressWarnings("SameParameterValue") int places) {
        if (places < 0) throw new IllegalArgumentException();

        return new BigDecimal(Double.toString(value)).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    private static double round(double value) {
        return round(value, 4);
    }


}
