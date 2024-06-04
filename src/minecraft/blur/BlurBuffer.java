package blur;


import kevin.utils.RenderUtils;

import java.awt.*;

public class BlurBuffer {

    public static void blurArea(float x, float y, float width, float height) {
        StencilUtil.initStencilToWrite();
        RenderUtils.drawRect(x, y, x + width, y + height, new Color(-2).getRGB());
        StencilUtil.readStencilBuffer(1);
        GaussianBlur.renderBlur(10);
        StencilUtil.uninitStencilBuffer();
    }
    public static void drawRectBlur(float x, float y, float x2, float y2) {
        StencilUtil.initStencilToWrite();
        RenderUtils.drawRect(x, y, x2, y2, new Color(-2).getRGB());
        StencilUtil.readStencilBuffer(1);
        GaussianBlur.renderBlur(10);
        StencilUtil.uninitStencilBuffer();
    }
    public static void drawRoundRectBlur(float x, float y, float x2, float y2, float cornerRadius,float scale) {
        StencilUtil.initStencilToWrite();
        RenderUtils.drawRoundedRect(x * scale, y * scale, x2 * scale, y2 * scale, cornerRadius, new Color(-2).getRGB(),true); // 绘制圆角矩形
        StencilUtil.readStencilBuffer(1);
        GaussianBlur.renderBlur(10);
        StencilUtil.uninitStencilBuffer();
    }


    public static void blurAreacustomradius(float x, float y, float width, float height ,float radius) {
        StencilUtil.initStencilToWrite();
        RenderUtils.drawRect(x, y, x + width, y + height, new Color(-2).getRGB());
        StencilUtil.readStencilBuffer(1);
        GaussianBlur.renderBlur(radius);
        StencilUtil.uninitStencilBuffer();
    }
}
