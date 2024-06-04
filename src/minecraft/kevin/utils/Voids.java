package kevin.utils;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.util.math.MathHelper.sqrt;


public class Voids {
    static final Minecraft mc = Minecraft.getMinecraft();
    public static void addPlayerMotionY(float y){
        mc.player.motionY += y;
    }

    public static void displayChatMessage(final String message) {
        if (mc.player == null) {
            return;
        }

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", message);

        ChatUtils.INSTANCE.message(jsonObject.toString());
    }





    public static Vec3d getVectorForRotation(final Vector2f rotation) {
        float yawCos = (float) Math.cos(-rotation.x * 0.017453292F - (float) Math.PI);
        float yawSin = (float) Math.sin(-rotation.x * 0.017453292F - (float) Math.PI);
        float pitchCos = (float) -Math.cos(-rotation.y * 0.017453292F);
        float pitchSin = (float) Math.sin(-rotation.y * 0.017453292F);
        return new Vec3d(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public static Vec3d getPlacePossibility(double offsetX, double offsetY, double offsetZ) {
        final List<Vec3d> possibilities = new ArrayList<>();
        final int range = (int) (5 + (Math.abs(offsetX) + Math.abs(offsetZ)));

        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = mc.world.getBlockState(new BlockPos(mc.player).add(x, y, z)).getBlock();
                    if (!(block instanceof BlockAir)) {
                        for (int x2 = -1; x2 <= 1; x2 += 2)
                            possibilities.add(new Vec3d(mc.player.posX + x + x2, mc.player.posY + y, mc.player.posZ + z));

                        for (int y2 = -1; y2 <= 1; y2 += 2)
                            possibilities.add(new Vec3d(mc.player.posX + x, mc.player.posY + y + y2, mc.player.posZ + z));

                        for (int z2 = -1; z2 <= 1; z2 += 2)
                            possibilities.add(new Vec3d(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z + z2));
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> mc.player.getDistance(vec3.x, vec3.y, vec3.z) > 5 || !(mc.world.getBlockState( new BlockPos(vec3.x, vec3.y, vec3.z) ).getBlock() instanceof BlockAir) || vec3.y > mc.player.posY);

        if (possibilities.isEmpty()) return null;

        possibilities.sort(Comparator.comparingDouble(vec3 -> {

            final double d0 = (mc.player.posX + offsetX) - vec3.x;
            final double d1 = (mc.player.posY - 1 + offsetY) - vec3.y;
            final double d2 = (mc.player.posZ + offsetZ) - vec3.z;
            return sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        }));

        return possibilities.get(0);
    }

    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }
}
