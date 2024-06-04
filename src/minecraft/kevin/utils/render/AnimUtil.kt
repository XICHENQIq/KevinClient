package kevin.utils.render

import net.minecraft.util.math.Vec2f

class AnimUtil {

    var realVec = Vec2f.ZERO

    fun animTo(vec2f: Vec2f) {
        realVec = vec2f
    }

    val animVec: Vec2f
        get() {
            var x = animVec.x
            var xTo = realVec.x
            var y = animVec.y
            var yTo = realVec.y
            if (x < xTo) {
                x += 0.01f
            }
            if (y < yTo) {
                y += 0.01f
            }
            if (x > xTo) {
                x -= 0.01f
            }
            if (y > yTo) {
                y -= 0.01f
            }
            return Vec2f(x,y)
        }
}