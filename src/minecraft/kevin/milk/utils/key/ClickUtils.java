package kevin.milk.utils.key;

public class ClickUtils {
    public static boolean isClickableWithRect(double x, double y, double dx, double dy, double mx, double my){
        return mx >= x && mx <= x + dx && my >= y && my <= y + dy;
    }
    public static boolean isClickable(double x, double y, double dx, double dy, double mx, double my){
        return mx >= x && mx <= dx && my >= y && my <= dy;
    }
    public static boolean isClickable(float x, float y, float dx, float dy, int mx, int my){
        return mx >= x && mx <= dx && my >= y && my <= dy;
    }
}
