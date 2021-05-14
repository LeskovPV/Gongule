package local.gongule.utils.colors;

import local.gongule.tools.ConfigFile;

import java.awt.*;

public class ColorSchema {

    static private ColorSchema instance = new ColorSchema(ConfigFile.getInstance().get("baseColor"));

    static public ColorSchema getInstance() {
        return instance;
    }

    private float baseRatio = 0.5f;

    //private HSLColor baseColor = new HSLColor("#7F7F7F"); //dark gray
    private HSLColor baseColor = new HSLColor("#6e7f88"); //dirty dark blue

    /**
     * Constructor
     */
    private ColorSchema() { }

    public ColorSchema(String baseColor) {
        setBaseColor(baseColor);
    }

    public ColorSchema(Color baseColor) {
        setBaseColor(baseColor);
    }

    public Color setBaseColor(Color baseColor) {
        if (baseColor != null) {
            this.baseColor = new HSLColor(baseColor);
            ConfigFile.getInstance().set("baseColor", this.baseColor.toHex());
        }
        return this.baseColor.getRGB();
    }

    public String setBaseColor(String baseColor) {
        if (baseColor != null)
        try {
            Color color = Color.decode(baseColor);
            this.baseColor = new HSLColor(color);
            ConfigFile.getInstance().set("baseColor", this.baseColor.toHex());
        } catch (Exception e) {}
        return this.baseColor.toHex();
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
