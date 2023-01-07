package dev.timecoding.dynamicsleeping.api.variables;

import dev.timecoding.dynamicsleeping.enums.DoubleOptionEnum;

public class DoubleOption {

    private String checkstring;

    public DoubleOption(String checkstring){
        this.checkstring = checkstring;
    }

    public Integer getAmount(){
        if(getType() == DoubleOptionEnum.AMOUNT){
            return Integer.valueOf(this.checkstring);
        }
        return 0;
    }

    public Integer getPercentage(){
        if(getType() == DoubleOptionEnum.PERCENTAGE){
            return Integer.valueOf(this.checkstring.replace("%", ""));
        }
        return 0;
    }

    public DoubleOptionEnum getType(){
        if(this.isInteger(this.checkstring)){
            return DoubleOptionEnum.AMOUNT;
        }else if(this.checkstring.contains("%")){
            String replace = this.checkstring.replace("%", "");
            if(isInteger(replace)){
                return DoubleOptionEnum.PERCENTAGE;
            }
        }
        return DoubleOptionEnum.UNKNOWN;
    }

    private boolean isInteger(String string){
        try{
            Integer.valueOf(string);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

}
