package local.gongule.tools.colors;

import java.awt.*;

public class ColorSchema {

    private float baseRatio = 0.5f;

    private HSLColor baseColor = new HSLColor("#7F7F7F");


    public ColorSchema() {
    }

    public ColorSchema(String baseColor) {
        setBaseColor(baseColor);
    }

    public ColorSchema(Color baseColor) {
        setBaseColor(baseColor);
    }

    public void setBaseColor(Color baseColor) {
        if (baseColor == null)
            return;
        this.baseColor = new HSLColor(baseColor);
    }

    public void setBaseColor(String baseColor) {
        if (baseColor == null)
            return;
        try {
            Color color = Color.decode(baseColor);
            this.baseColor = new HSLColor(color);
        } catch (Exception e) {}
    }

    public String getBaseColor() {
        return baseColor.toHex();
    }

    public String getSiteColor() {
        return adjustMainLuminance(baseRatio / 10).toHex();
    }

    public String getDeepColor() {
        return adjustMainLuminance(baseRatio).toHex();
    }

    private HSLColor adjustMainLuminance(float ratio) {
        float mainLuminance = baseColor.getLuminance();
        float luminance = mainLuminance < 50f ? mainLuminance * ratio : 100 - (100 - mainLuminance) * ratio;
        float minLuminance = 0;
        float maxLuminance = 100;
        if (luminance < minLuminance) luminance = minLuminance;
        if (luminance > maxLuminance) luminance = maxLuminance;
        return new HSLColor(baseColor.adjustLuminance(luminance));
    }

    public String getTextColor() {
        float luminance = 100 - new HSLColor(Color.decode(getSiteColor())).getLuminance();
        return new HSLColor(baseColor.adjustLuminance(luminance)).toHex();
    }

    public String getHalfColor() {
        return new HSLColor(baseColor.adjustLuminance(50f)).toHex();
    }

}
