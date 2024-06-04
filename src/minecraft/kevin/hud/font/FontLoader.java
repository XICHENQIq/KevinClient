package kevin.hud.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;

public class FontLoader {
    public UFontRenderer arial14;
    public UFontRenderer arial16;
    public UFontRenderer arial18;
    public UFontRenderer arial22;
    public UFontRenderer arial24;
    public UFontRenderer client18;
    public UFontRenderer client36;
    public UFontRenderer client72;
    public FontRenderer original;

    private static final HashMap<Integer, UFontRenderer> cFonts = new HashMap<>();
    private static final HashMap<Integer, UFontRenderer> cFontsBold = new HashMap<>();
    private static final HashMap<Integer, UFontRenderer> aFonts = new HashMap<>();


    public FontLoader() {
        long t1 = System.currentTimeMillis();
        original = Minecraft.getMinecraft().fontRendererObjWithoutUnicode;
        arial14 = getArial(14, true);
        arial16 = getArial(16, true);
        arial18 = getArial(18, true);
        arial22 = getArial(22, true);
        arial24 = getArial(24, true);
        client18 = getClientFont(18, true);
        client36 = getClientFont(36, true);
        client72 = getClientFont(72, true);

        for (int i = 10; i < 37; i++) {
            cFonts.put(i, getClientFont(i, true));
            cFontsBold.put(i, getClientBold(i, true));
            aFonts.put(i, getArial(i, true));
            i++;
        }

    }

    public static UFontRenderer getCFont(boolean bold, int size) {
        if (!bold) {
            if(cFonts.get(size) == null) {
                cFonts.put(size, getClientFont(size, false));
            }
            return cFonts.get(size);
        } else {
            if(cFontsBold.get(size) == null) {
                cFontsBold.put(size, getClientBold(size, false));
            }
            return cFontsBold.get(size);
        }
    }

    public static UFontRenderer getAFont(int size) {
        return aFonts.get(size);
    }


    private static UFontRenderer getClientFont(int size, boolean antiAlias) {
        return getFont("regular.ttf", size, antiAlias, false);
    }

    private static UFontRenderer getClientBold(int size, boolean antiAlias) {
        return getFont("bold.ttf", size, antiAlias, false);
    }

    public static UFontRenderer getFont(String fontName, int size, boolean antiAlias, boolean bold) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(fontName)).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(bold ? Font.BOLD : Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            font = new Font("default", bold ? Font.BOLD : Font.PLAIN, size);
        }

        return new UFontRenderer(font, size, antiAlias);
    }

    public UFontRenderer getArial(int size, boolean antiAlias) {
        return getFont("arial.ttf", size, antiAlias, false);
    }

}

