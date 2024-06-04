package kevin.utils.skid;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class EnumFacingOffset {
    public EnumFacing enumFacing;
    public Vec3d offset;

    public EnumFacingOffset(final EnumFacing enumFacing, final Vec3d offset) {
        this.enumFacing = enumFacing;
        this.offset = offset;
    }
}
