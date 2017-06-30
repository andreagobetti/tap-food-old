package com.lynkteam.tapmanager.util;

/**
 * Created by robertov on 28/08/15.
 */
public class DecimalUtil {

    //prende un int nel formato eurocent e lo restituisce formattato
    public static String formatInt(int amount){
        String am = Integer.toString(amount);
        while(am.length()<3)
            am = "0" + am;
        am = am.substring(0, am.length()-2) +"." + am.substring(am.length()-2);
        return am;
    }

    //prende una stringa nel formato euro.cent o euro,cent e la formatta nel formato eurocent troncando a due cifre decimali
    public static int formatString(String amount){
        if (amount.contains(",")) {
            amount = amount + "00";
            int p = amount.indexOf(",");

            amount = amount.substring(0, p) + amount.substring(p + 1).substring(0, 2);
        }else if (amount.contains(".")) {
            amount = amount + "00";

            int p = amount.indexOf(".");

            amount = amount.substring(0, p) + amount.substring(p + 1).substring(0, 2);
        } else {
            amount = amount + "00";
        }

        return  Integer.parseInt(amount);
    }
}
