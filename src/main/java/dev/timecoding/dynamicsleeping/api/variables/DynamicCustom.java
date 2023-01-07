package dev.timecoding.dynamicsleeping.api.variables;

import dev.timecoding.dynamicsleeping.enums.IfType;

public class DynamicCustom {

    private String ifOption;
    private boolean exactly;
    private boolean everytime;
    private boolean increaseEnabled;
    private String increaseSpeed;
    private String increaseTicks;
    private Integer changeSpeed;
    private Integer perTicks;

    public DynamicCustom(String ifOption, Boolean exactly, Boolean everytime, Boolean increaseEnabled, String increaseSpeed, String increaseTicks, Integer changeSpeed, Integer perTicks){
        this.ifOption = ifOption;
        this.exactly = exactly;
        this.everytime = everytime;
        this.increaseEnabled = increaseEnabled;
        this.increaseSpeed = increaseSpeed;
        this.increaseTicks = increaseTicks;
        this.changeSpeed = changeSpeed;
        this.perTicks = perTicks;
    }

    public boolean isEverytime() {
        return everytime;
    }

    public boolean isExactly() {
        return exactly;
    }

    public boolean isIncreaseEnabled() {
        return increaseEnabled;
    }

    public Integer getChangeSpeed() {
        return changeSpeed;
    }

    public String getStringIfOption() {
        return ifOption;
    }

    public IfOption getIfOption(){
        return new IfOption(this.getStringIfOption());
    }

    public String getIncreaseSpeedString() {
        return increaseSpeed;
    }

    public DoubleOption getIncreaseSpeedOption(){
        return new DoubleOption(getIncreaseSpeedString());
    }

    public DoubleOption getIncreaseTicksOption(){
        return new DoubleOption(getIncreaseTicksString());
    }

    public String getIncreaseTicksString() {
        return increaseTicks;
    }

    public Integer getPerTicks() {
        return perTicks;
    }
}
