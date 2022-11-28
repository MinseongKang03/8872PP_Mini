package org.firstinspires.ftc.teamcode.command.lift;

import android.util.Log;
import com.arcrobotics.ftclib.command.CommandBase;
import org.firstinspires.ftc.teamcode.subsystem.ArmSubsystem;

public class MoveToLocation extends CommandBase{
    private final ArmSubsystem arm;

    public MoveToLocation(ArmSubsystem arm) {
        this.arm = arm;
        addRequirements(arm);
    }

    @Override
    public void execute() {
        arm.loopPID();
        arm.resetEncoders();
    }
    @Override
    public boolean isFinished() {
        Log.d("MoveToLocation status", "finished");
        return arm.atTarget();
    }
}
