package newbank.server;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import static java.lang.Integer.parseInt;

public class ourCurrency{

    public ourCurrency(String pounds){
        //
    }

    //Run assuming that decimalValid method has been run prior
    public static int convertToPennies (String pounds) throws NumberFormatException{
        if (!moneyValid(pounds)) {
            throw new NumberFormatException("Incorrect monetary value. Please use format #.##");
        }
            int pennies = 0;
            int count = 0;
            //This finds where the decimal point is. This is assuming moneyValid method has been run
            for (int index = pounds.length() - 1; index >= pounds.length() - 3; index--) {
                //Checks that index is still in range to run this code - i.e. needed when pounds.length < 3
                if (index < 0) {
                    count = -1;
                    break;
                }
                if (pounds.charAt(index) == '.') {
                    break;
                }
                count++;
                //If no decimal point
                if (index == pounds.length() - 3 & pounds.charAt(index) != '.') {
                    count = -1;
                }
            }

            //Split money at decimal point
            String[] splitMoney = pounds.split("\\.");

            //If no DP at all
            if (count == -1) {
                pennies = parseInt(pounds) * 100;
            }
            //If DP at end with no pennies
            if (count == 0) {
                pennies = parseInt(splitMoney[0]) * 100;
            }
            //If DP has 1 number after it
            if (count == 1) {
                pennies = parseInt(splitMoney[0]) * 100 + parseInt(splitMoney[1]) * 10;
            }
            //If DP has 2 numbers after it and not the first character
            if (count == 2 & pounds.length()>3) {
                pennies = parseInt(splitMoney[0]) * 100 + parseInt(splitMoney[1]);
            }
            //If DP has 2 numbers after is and is the first character
            if (count == 2 & pounds.length() == 3){
                pennies = parseInt(splitMoney[0]);
            }
            return pennies;
    }

    public static String printMoney(int pennies){
        BigDecimal pounds = new BigDecimal((double) pennies / 100);
        Locale gb = new Locale("en", "GB");
        Currency gbp = Currency.getInstance(gb);
        NumberFormat gbpFormat = NumberFormat.getCurrencyInstance(gb);
        return (gbpFormat.format(pounds));
    }

    //Use this to throw errors when string for money isn't valid
    public static boolean moneyValid (String pounds){
        /*Find decimal point
        * Count makes sure there is only 1 dp in the string
        * Position keeps track of where in the string the dp is
        * Index just used to move through string*/
        int count = 0;
        int position = 0;
        for (int index = 0; index < pounds.length(); index++) {
            /*If the character is not an integer - check if it's a decimal point.
            * If it's not a dp or an integer, this is not a valid string
             */
            if (!Character.isDigit(pounds.charAt(index))) {
                if (pounds.charAt(index) == '.') {
                    count++;
                    position = index;
                    if (count > 1) {
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
        }

        /*Now checking that the dp is in the right place for it to be a valid string*/
        //If decimal doesn't exist, integer valid, return true
        if (position == 0 & pounds.charAt(position)!='.') {
            return true;
        }
        //If decimal is in the last 3 positions in the string, return true
        //Otherwise decimal is in invalid place for money - return false
        return position <= pounds.length() - 1 & position >= pounds.length() - 3;
    }
}
