package br.socket.domain;

import java.util.List;


public class Utils {
    public static double calculateStandardDeviation(List<Double> mrts) {

        // get the sum of array
        double sum = 0.0;
        for (double i : mrts) {
            sum += i;
        }

        // get the mean of array
        int length = mrts.size();
        double mean = sum / length;

        // calculate the standard deviation
        double standardDeviation = 0.0;
        for (double num : mrts) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

    public static String registerTime(String receivedMessage) {
        String lastRegisteredTimeStampString;
        String[] stringSplited;

        stringSplited = receivedMessage.split(";");
        lastRegisteredTimeStampString = stringSplited[stringSplited.length - 1];
        long timeNow = System.currentTimeMillis();
        receivedMessage += timeNow + ";"+ (timeNow- Long.parseLong(lastRegisteredTimeStampString.trim()))+";";
//        receivedMessage += ";"+timeNow + ";"+ (timeNow- Long.parseLong(lastRegisteredTimeStampString.trim()))+";";

        return receivedMessage;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
