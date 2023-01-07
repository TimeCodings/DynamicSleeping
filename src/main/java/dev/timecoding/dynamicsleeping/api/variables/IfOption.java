package dev.timecoding.dynamicsleeping.api.variables;

import dev.timecoding.dynamicsleeping.enums.IfType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IfOption {

    private String ifstring;

    public IfOption(String ifstring){
        this.ifstring = ifstring;
    }

    public Integer getAmount(){
        if(getType() == IfType.AMOUNT){
            return Integer.valueOf(this.ifstring);
        }
        return 0;
    }

    public Integer getPercentage(){
        if(getType() == IfType.PERCENTAGE){
            return Integer.valueOf(this.ifstring.replace("%", ""));
        }
        return 0;
    }

    public List<Integer> getSplittedAmount(){
        if(getType() == IfType.AMOUNTSPLITTED){
            ArrayList<String> converter = new ArrayList<>(Arrays.asList(this.ifstring.split("/")));
            List<Integer> list = new ArrayList<>();
            for(String s : converter){
                list.add(Integer.valueOf(s));
            }
            return list;
        }
        return new ArrayList<>();
    }

    public IfType getType(){
        if(isInteger(this.ifstring)){
            return IfType.AMOUNT;
        }else if(this.ifstring.endsWith("%") && isInteger(this.ifstring.replace("%", ""))){
            return IfType.PERCENTAGE;
        }else if(this.ifstring.contains("/")){
            ArrayList<String> list = new ArrayList<>(Arrays.asList(this.ifstring.split("/")));
            if(list.size() == 2 && isInteger(list.get(0)) && isInteger(list.get(1))){
                return IfType.AMOUNTSPLITTED;
            }
        }
        return IfType.UNKNOWN;
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
