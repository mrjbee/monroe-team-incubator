package org.monroe.team.smooker.app.uc.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

public class DateUtils {

    public static Date dateOnly(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
           return formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            throw new RuntimeException();
        }
    }

    public static Date getNow(){
       return new Date();
    }
}
