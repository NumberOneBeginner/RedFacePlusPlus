package com.example.peter.redfaceplusplus.selecttime;

import java.util.ArrayList;

/**
 * Created by santa on 16/7/19.
 */
public class SelectorContanst {
    private static SelectorContanst instance = null;
    public static ArrayList<String> MONTHS = null;
    public static ArrayList<String> YEARS = null;
    public static ArrayList<String> DAYS = null;
    public static ArrayList<String> HOURS = null;
    public static ArrayList<String> MINS = null;


    public static ArrayList<String> getMonths() {
        if (null == MONTHS) {
            synchronized (SelectorContanst.class){
                MONTHS = new ArrayList<>();
                for (int i = 1 ; i<=12; i++) {
                    MONTHS.add(i+"month");
                }
            }
        }
        return MONTHS;
    }


    public static ArrayList<String> getYears() {
        if (null == YEARS) {
            synchronized (SelectorContanst.class){
                YEARS = new ArrayList<>();
                for (int i = 1900 ; i<=3000; i++) {
                    YEARS.add(i+"year");
                }
            }
        }
        return YEARS;
    }


    public static ArrayList<String> getDays() {
        if (null == DAYS) {
            synchronized (SelectorContanst.class){
                DAYS = new ArrayList<>();
                for (int i = 1 ; i<=31; i++) {
                    DAYS.add(i+"day");
                }
            }
        }
        return DAYS;
    }


    public static ArrayList<String> getHours() {
        if (null == HOURS) {
            synchronized (SelectorContanst.class){
                HOURS = new ArrayList<>();
                for (int i = 0 ; i<=23; i++) {
                    HOURS.add(i+"hour");
                }
            }
        }
        return HOURS;
    }


    public static ArrayList<String> getMins() {
        if (null == MINS) {
            synchronized (SelectorContanst.class){
                String[] fenzhong_start =
                        { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18",
                                "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36",
                                "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54",
                                "55", "56", "57", "58", "59" };
                MINS = new ArrayList<>();
                for (int i=0;i<fenzhong_start.length-1;i++){
                    MINS.add(fenzhong_start[i]+"minute");
                }
//                for (int i = 0 ; i<=59; i++) {
//                    MINS.add(i+"minute");
//                }
            }
        }
        return MINS;
    }

}
