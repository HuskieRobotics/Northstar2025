// Copyright (c) 2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.

package org.littletonrobotics.frc2025.subsystems.superstructure;

import static org.littletonrobotics.frc2025.subsystems.superstructure.SuperstructureConstants.*;

import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.littletonrobotics.frc2025.FieldConstants;
import org.littletonrobotics.frc2025.FieldConstants.ReefLevel;
import org.littletonrobotics.frc2025.RobotState;
import org.littletonrobotics.frc2025.util.GeomUtil;
import org.littletonrobotics.frc2025.util.LoggedTunableNumber;

public record SuperstructurePose(DoubleSupplier elevatorHeight, Supplier<Rotation2d> pivotAngle) {
  private static final double reefAlgaeIntakeAngle = 15.0;

  private static final Map<ReefLevel, Pair<Double, Double>> ejectMeters =
      Map.of(
          ReefLevel.L1,
          Pair.of(Units.inchesToMeters(15.0), Units.inchesToMeters(15.0)),
          ReefLevel.L2,
          Pair.of(Units.inchesToMeters(5.0), Units.inchesToMeters(12.0)),
          ReefLevel.L3,
          Pair.of(Units.inchesToMeters(5.0), Units.inchesToMeters(8.0)),
          ReefLevel.L4,
          Pair.of(Units.inchesToMeters(5.0), Units.inchesToMeters(6.0)));
  private static final Map<ReefLevel, Pair<Double, Double>> optimalCoralAngles =
      Map.of(
          ReefLevel.L1,
          //     No Algae  Algae (Need to flip will reverse)
          Pair.of(40.0, -40.0),
          ReefLevel.L2,
          Pair.of(-35.0, -30.0),
          ReefLevel.L3,
          Pair.of(-35.0, -30.0),
          ReefLevel.L4,
          Pair.of(-60.0, -40.0));

  @Getter
  @RequiredArgsConstructor
  /**
   * Dispenser pose relative to branch on ground (looking at the robot from the left side) Rotation
   * is just the rotation of dispenser when scoring.
   */
  public enum DispenserPose {
    L1(
        new Pose2d(
            Units.inchesToMeters(15.0),
            dispenserOrigin2d.getY(),
            Rotation2d.fromDegrees(optimalCoralAngles.get(ReefLevel.L1).getFirst()))),
    L2(ReefLevel.L2, false),
    L3(ReefLevel.L3, false),
    L4(ReefLevel.L4, false),
    L1_ALGAE(ReefLevel.L1, true),
    L2_ALGAE(ReefLevel.L2, true),
    L3_ALGAE(ReefLevel.L3, true),
    L4_ALGAE(ReefLevel.L4, true),
    L2_ALGAE_INTAKE(new Pose2d(getAlgaeIntakeTranslation(true), Rotation2d.fromDegrees(30.0))),
    L3_ALGAE_INTAKE(new Pose2d(getAlgaeIntakeTranslation(false), Rotation2d.fromDegrees(30.0)));

    private ReefLevel reefLevel = ReefLevel.L1;
    private boolean algae = false;
    private final Pose2d pose;

    DispenserPose(ReefLevel ReefLevel, boolean algae) {
      this.reefLevel = ReefLevel;
      this.algae = algae;
      // Calculate dispenser pose relative to appropriate branch
      double dispenserAngleDeg =
          algae
              ? optimalCoralAngles.get(ReefLevel).getSecond()
              : optimalCoralAngles.get(ReefLevel).getFirst();
      pose =
          new Pose2d(
              new Pose2d(0.0, ReefLevel.height, Rotation2d.fromDegrees(-dispenserAngleDeg))
                  .transformBy(
                      GeomUtil.toTransform2d(
                          (algae
                                  ? ejectMeters.get(ReefLevel).getSecond()
                                  : ejectMeters.get(ReefLevel).getFirst())
                              + (algae ? pivotToTunnelBack : pivotToTunnelFront),
                          0.0))
                  .getTranslation(),
              Rotation2d.fromDegrees(algae ? dispenserAngleDeg - 180.0 : dispenserAngleDeg));
    }

    public double getElevatorHeight() {
      return (pose.getY() - dispenserOrigin2d.getY()) / elevatorAngle.getSin();
    }

    public double getDispenserAngleDeg() {
      return pose.getRotation().getDegrees();
    }

    public static DispenserPose fromReefLevel(ReefLevel reefLevel, boolean algae) {
      return switch (reefLevel) {
        case L1 -> algae ? DispenserPose.L1_ALGAE : DispenserPose.L1;
        case L2 -> algae ? DispenserPose.L2_ALGAE : DispenserPose.L2;
        case L3 -> algae ? DispenserPose.L3_ALGAE : DispenserPose.L3;
        case L4 -> algae ? DispenserPose.L4_ALGAE : DispenserPose.L4;
      };
    }

    public static DispenserPose fromAlgaeObjective(FieldConstants.AlgaeObjective objective) {
      return (objective.id() % 2 == 0)
          ? DispenserPose.L3_ALGAE_INTAKE
          : DispenserPose.L2_ALGAE_INTAKE;
    }

    private static Translation2d getAlgaeIntakeTranslation(boolean low) {
      return new Pose2d(
              new Pose2d(
                      0.0,
                      ReefLevel.L3.height
                          + (low ? -1.0 : 1.0) * (ReefLevel.L3.height - ReefLevel.L2.height) / 2.0,
                      Rotation2d.fromDegrees(-180.0 - ReefLevel.L3.pitch)) // Flip pitch into frame
                  .transformBy(GeomUtil.toTransform2d(FieldConstants.algaeDiameter / 2.0, 0.0))
                  .getTranslation(),
              Rotation2d.fromDegrees(-reefAlgaeIntakeAngle))
          .transformBy(
              GeomUtil.toTransform2d(
                  FieldConstants.algaeDiameter / 2.0
                      + pivotToTunnelFront
                      + Units.inchesToMeters(3.0),
                  0.0))
          .getTranslation();
    }
  }

  // Read distance to branch from robot state to calculate positions
  @RequiredArgsConstructor
  @Getter
  enum Preset {
    STOW("Stow", 0.0, 10.0),
    L1("L1", 0.15, DispenserPose.L1.getDispenserAngleDeg()),
    L2(ReefLevel.L2, false),
    L3(ReefLevel.L3, false),
    L4(ReefLevel.L4, false),
    ALGAE_FLOOR_INTAKE("AlgaeFloorIntake", 0.4, -20.0),
    ALGAE_L2_INTAKE("AlgaeL2Intake", DispenserPose.L2_ALGAE_INTAKE),
    ALGAE_L3_INTAKE("AlgaeL3Intake", DispenserPose.L3_ALGAE_INTAKE),
    THROW(() -> elevatorMaxTravel, () -> 40.0),
    POST_PRE_PROCESSOR("Processing", 0.28, -15.0),
    ALGAE_STOW(
        "AlgaeStow",
        Units.inchesToMeters(33.0) - dispenserToTop - bottomToDispenser,
        pivotSafeAngle.getDegrees()),
    L1_CORAL_REVERSED(ReefLevel.L1, true),
    L2_CORAL_REVERSED(ReefLevel.L2, true),
    L3_CORAL_REVERSED(ReefLevel.L3, true),
    L4_CORAL_REVERSED(ReefLevel.L4, true);

    private final SuperstructurePose pose;

    Preset(DoubleSupplier elevatorHeight, DoubleSupplier pivotAngle) {
      this(
          new SuperstructurePose(
              elevatorHeight, () -> Rotation2d.fromDegrees(pivotAngle.getAsDouble())));
    }

    Preset(String name, DispenserPose dispenserPose) {
      this(name, dispenserPose.getElevatorHeight(), dispenserPose.getDispenserAngleDeg());
    }

    Preset(String name, double elevatorHeight, double pivotAngle) {
      this(
          new LoggedTunableNumber("Superstructure/" + name + "/Elevator", elevatorHeight),
          new LoggedTunableNumber("Superstructure/" + name + "/Pivot", pivotAngle));
    }

    Preset(ReefLevel ReefLevel, boolean algae) {
      var dispenserPose = DispenserPose.fromReefLevel(ReefLevel, algae);
      var elevatorHeight =
          new LoggedTunableNumber(
              "Superstructure/" + ReefLevel + (algae ? "Algae" : "") + "/Elevator",
              dispenserPose.getElevatorHeight());
      pose =
          new SuperstructurePose(
              elevatorHeight,
              () ->
                  RobotState.getInstance().getDistanceToBranch().isEmpty()
                      ? Rotation2d.fromDegrees(dispenserPose.getDispenserAngleDeg())
                      : Rotation2d.fromDegrees(
                          new Rotation2d(
                                      RobotState.getInstance().getDistanceToBranch().getAsDouble(),
                                      ReefLevel.height
                                          - (elevatorHeight.get() * elevatorAngle.getSin()
                                              + dispenserOrigin2d.getY()))
                                  .getDegrees()
                              + (algae ? -180.0 : 0.0)));
    }
  }

  SuperstructurePose() {
    this(() -> 0.0, () -> Rotation2d.kZero);
  }
}
