package org.firstinspires.ftc.teamcode.opmode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.button.GamepadButton;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.hardware.RevIMU;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.outoftheboxrobotics.photoncore.PhotonCore;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.TouchSensor;
import org.firstinspires.ftc.teamcode.subsystem.ClawSubsystem;
import org.firstinspires.ftc.teamcode.subsystem.LiftSubsystem;
import org.firstinspires.ftc.teamcode.subsystem.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystem.SlideSubsystem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class BaseOpMode extends CommandOpMode {
    protected MotorEx fL, fR, bL, bR, dr4bLeftMotor, dr4bRightMotor;
    protected SimpleServo clawServo, slideServo;
    protected DriveSubsystem drive;
    protected LiftSubsystem lift;
    protected ClawSubsystem claw;
    protected SlideSubsystem slide;
    protected RevIMU imu;
    protected TouchSensor limitSwitch;

    protected GamepadEx gamepadEx1;
    protected GamepadEx gamepadEx2;

    private final boolean usePhoton;
    private final boolean useBulkRead;

    protected BaseOpMode(boolean usePhoton, boolean useBulckRead) {
        this.usePhoton = usePhoton;
        this.useBulkRead = useBulckRead;
    }

    @Override
    public void initialize() {
        gamepadEx1 = new GamepadEx(gamepad1);
        gamepadEx2 = new GamepadEx(gamepad2);

        //Photon ftc enabled
        if (usePhoton) PhotonCore.enable();

        //bulk read set to auto when true
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

        if (useBulkRead) {
            for (LynxModule module : allHubs) {
                module.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
            }
        }

        initHardware();
        setUpHardwareDevices();

        imu = new RevIMU(hardwareMap);
        imu.init();

        drive = new DriveSubsystem(fL, fR, bL, bR, imu);
        lift = new LiftSubsystem(dr4bLeftMotor, dr4bRightMotor, limitSwitch);
        claw = new ClawSubsystem(clawServo);
        slide = new SlideSubsystem(slideServo);


        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
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
        clawServo = new SimpleServo(hardwareMap, "claw", 0, 120);
        slideServo = new SimpleServo(hardwareMap, "slide", 0, 120);
        slideServo.setPosition(SlideSubsystem.inPosition);
        limitSwitch = hardwareMap.get(TouchSensor.class, "touch");
        dr4bLeftMotor.resetEncoder();
        dr4bRightMotor.resetEncoder();
    }


    @Override
    public void run() {
        super.run();

        tad("leftFront Power", round(fL.motor.getPower()));
        tad("leftBack Power", round(bL.motor.getPower()));
        tad("rightFront Power", round(fR.motor.getPower()));
        tad("rightBack Power", round(bR.motor.getPower()));
        tad("dr4bLeftMotor Power", round(dr4bLeftMotor.motor.getPower()));
        tad("dr4bRightMotor Power", round(dr4bRightMotor.motor.getPower()));
        tad("dr4bLeftMotor Position", dr4bLeftMotor.getCurrentPosition());
        tad("dr4bRightMotor Position", dr4bRightMotor.getCurrentPosition());

        tad("Drive Heading PID Output", drive.getOutput());
        tad("target", drive.getTarget());

        tad("claw Position", clawServo.getPosition());
        tad("slide Position", slideServo.getPosition());

        tad("IMU Heading", imu.getAbsoluteHeading());

        tad("Limit Pressed", limitSwitch.isPressed());
        telemetry.update();
    }

    protected void setUpHardwareDevices() {
        fL.setInverted(true);
        bL.setInverted(true);
        fL.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        fR.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        bL.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        bR.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);

        dr4bLeftMotor.setRunMode(Motor.RunMode.RawPower);
        dr4bRightMotor.setRunMode(Motor.RunMode.RawPower);
        dr4bLeftMotor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        dr4bRightMotor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
    }


    private static double round(double value, @SuppressWarnings("SameParameterValue") int places) {
        if (places < 0) throw new IllegalArgumentException();

        return new BigDecimal(Double.toString(value)).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    private static double round(double value) {
        return round(value, 4);
    }

    // gamepad button 1 = gb1
    protected GamepadButton gb1(GamepadKeys.Button button){
        return gamepadEx1.getGamepadButton(button);
    }

    // gamepad button 2 = gb2
    protected GamepadButton gb2(GamepadKeys.Button button){
        return gamepadEx2.getGamepadButton(button);
    }

    // telemetry add data = tad
    protected void tad(String caption, Object value){
        telemetry.addData(caption, value);
    }
}
