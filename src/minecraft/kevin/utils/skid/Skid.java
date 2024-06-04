package kevin.utils.skid;

import com.google.common.base.Predicates;
import kevin.utils.Mc;
import kevin.utils.Rotation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;

import java.util.*;

import static kevin.utils.RotationUtils.getVectorForRotation;
import static kevin.utils.skid.ExtensionKt.rayTraceCustom;


public final class Skid extends Mc {
    public static Vec3d getPlacePossibility(double offsetX, double offsetY, double offsetZ) {
        final List<Vec3d> possibilities = new ArrayList<>();
        final int range = (int) (5 + (Math.abs(offsetX) + Math.abs(offsetZ)));

        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = Skid.blockRelativeToPlayer(x, y, z);

                    if (!(block instanceof BlockAir)) {
                        for (int x2 = -1; x2 <= 1; x2 += 2)
                            possibilities.add(new Vec3d(getMc().player.posX + x + x2, getMc().player.posY + y, getMc().player.posZ + z));

                        for (int y2 = -1; y2 <= 1; y2 += 2)
                            possibilities.add(new Vec3d(getMc().player.posX + x, getMc().player.posY + y + y2, getMc().player.posZ + z));

                        for (int z2 = -1; z2 <= 1; z2 += 2)
                            possibilities.add(new Vec3d(getMc().player.posX + x, getMc().player.posY + y, getMc().player.posZ + z + z2));
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> getMc().player.getDistance(vec3.x, vec3.y, vec3.z) > 5 || !(Skid.block(vec3.x, vec3.y, vec3.z) instanceof BlockAir));

        if (possibilities.isEmpty()) return null;

        possibilities.sort(Comparator.comparingDouble(vec3 -> {

            final double d0 = (getMc().player.posX + offsetX) - vec3.x;
            final double d1 = (getMc().player.posY - 1 + offsetY) - vec3.y;
            final double d2 = (getMc().player.posZ + offsetZ) - vec3.z;
            return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        }));

        return possibilities.get(0);
    }
    public static Vec3d getPlacePossibility2(double offsetX, double offsetY, double offsetZ) {
        final List<Vec3d> possibilities = new ArrayList<>();
        final EntityPlayerSP player = getMc().player;
        final double playerPosX = player.posX;
        final double playerPosY = player.posY;
        final double playerPosZ = player.posZ;
        final int range = (int) (5 + (Math.abs(offsetX) + Math.abs(offsetZ)));

        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    if (!(Skid.blockRelativeToPlayer(x, y, z) instanceof BlockAir)) {
                        for (int i = -1; i <= 1; i += 2) {
                            possibilities.add(new Vec3d(playerPosX + x + i, playerPosY + y, playerPosZ + z));
                            possibilities.add(new Vec3d(playerPosX + x, playerPosY + y + i, playerPosZ + z));
                            possibilities.add(new Vec3d(playerPosX + x, playerPosY + y, playerPosZ + z + i));
                        }
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> player.getDistance(vec3.x, vec3.y, vec3.z) > 5 || (Skid.block(vec3.x, vec3.y, vec3.z) instanceof BlockAir));

        if (possibilities.isEmpty()) return null;

        return possibilities.stream().min(Comparator.comparingDouble(vec3 -> {
            final double d0 = (playerPosX + offsetX) - vec3.x;
            final double d1 = (playerPosY - 1 + offsetY) - vec3.y;
            final double d2 = (playerPosZ + offsetZ) - vec3.z;
            return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        })).orElse(null);
    }

    public static Vec3d getPlacePossibilityCustomY(double offsetX, double customY, double offsetZ) {
        final List<Vec3d> possibilities = new ArrayList<>();
        final int range = (int) (5 + (Math.abs(offsetX) + Math.abs(offsetZ)));

        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = Skid.blockRelativeToPlayer(x, y, z);

                    if (!(block instanceof BlockAir)) {
                        for (int x2 = -1; x2 <= 1; x2 += 2)
                            possibilities.add(new Vec3d(getMc().player.posX + x + x2, getMc().player.posY + y, getMc().player.posZ + z));

                        for (int y2 = -1; y2 <= 1; y2 += 2)
                            possibilities.add(new Vec3d(getMc().player.posX + x, getMc().player.posY + y + y2, getMc().player.posZ + z));

                        for (int z2 = -1; z2 <= 1; z2 += 2)
                            possibilities.add(new Vec3d(getMc().player.posX + x, getMc().player.posY + y, getMc().player.posZ + z + z2));
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> getMc().player.getDistance(vec3.x, vec3.y, vec3.z) > 5 || !(Skid.block(vec3.x, vec3.y, vec3.z) instanceof BlockAir));

        if (possibilities.isEmpty()) return null;

        possibilities.sort(Comparator.comparingDouble(vec3 -> {

            final double d0 = (getMc().player.posX + offsetX) - vec3.x;
            final double d1 = (customY) - vec3.y;
            final double d2 = (getMc().player.posZ + offsetZ) - vec3.z;
            return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        }));

        return possibilities.get(0);
    }


    /**
     * Gets the block relative to the player from the offset
     *
     * @return block relative to the player
     */
    public static Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return getMc().world.getBlockState(new BlockPos(getMc().player).add(offsetX, offsetY, offsetZ)).getBlock();
    }

    /**
     * Gets the block at a position
     *
     * @return block
     */
    public static Block block(final double x, final double y, final double z) {
        return getMc().world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static EnumFacingOffset getEnumFacing(final Vec3d position) {
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (!(block(position.x + x2, position.y, position.z) instanceof BlockAir)) {
                if (x2 > 0) {
                    return new EnumFacingOffset(EnumFacing.WEST, new Vec3d(x2, 0, 0));
                } else {
                    return new EnumFacingOffset(EnumFacing.EAST, new Vec3d(x2, 0, 0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!(block(position.x, position.y + y2, position.z) instanceof BlockAir)) {
                if (y2 < 0) {
                    return new EnumFacingOffset(EnumFacing.UP, new Vec3d(0, y2, 0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!(block(position.x, position.y, position.z + z2) instanceof BlockAir)) {
                if (z2 < 0) {
                    return new EnumFacingOffset(EnumFacing.SOUTH, new Vec3d(0, 0, z2));
                } else {
                    return new EnumFacingOffset(EnumFacing.NORTH, new Vec3d(0, 0, z2));
                }
            }
        }

        return null;
    }

    public static boolean overBlock(final Rotation rotation, final EnumFacing enumFacing, final BlockPos pos, final boolean strict) {
        final RayTraceResult movingObjectPosition = rayTraceCustom(Objects.requireNonNull(getMc().player),4.5f, rotation.getYaw(), rotation.getPitch());

        return Objects.equals(movingObjectPosition.getBlockPos(), pos) && (!strict || movingObjectPosition.sideHit == enumFacing);
    }


}
