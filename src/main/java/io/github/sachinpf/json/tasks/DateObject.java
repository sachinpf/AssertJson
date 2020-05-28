package io.github.sachinpf.json.tasks;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateObject {

    private boolean ignoreSeconds;

    public DateObject(boolean ignoreSeconds) {
        this.ignoreSeconds = ignoreSeconds;
    }

    //matches given string with date time format
    //needs only time pattern fix
    public Object getDateIfDate(String dateTime) {
        //Pattern 1 - Matching format: "2018-08-30T06:28:13Z"
        {
            String re1 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))[-:\\/.](?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
            String re2 = "([a-z])";    // Any Single Word Character (Not Whitespace) 1
            String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re4 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
            String re5 = "(Z)";    // Any Single Word Character (Not Whitespace) 2

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);

            if (m.find()) {
                String date = m.group(1);
                String time;
                if (ignoreSeconds)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                return LocalDateTime.parse(date + "T" + time);
            }
        }

        //Pattern 2 - Matching format: "2019-02-16T06:38:49.349Z"
        {
            String re1 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))[-:\\/.](?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
            String re2 = "([a-z])";    // Any Single Word Character (Not Whitespace) 1
            String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re4 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
            // String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // HourMinuteSec 1
            String re5 = "(\\.)";    // Any Single Character 1
            String re6 = "(\\d)";    // Any Single Digit 1
            String re7 = "(\\d)";    // Any Single Digit 2
            String re8 = "(\\d)";    // Any Single Digit 3
            String re9 = "(Z)";    // Any Single Word Character (Not Whitespace) 2

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String date = m.group(1);
                String time;
                if (ignoreSeconds)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                return LocalDateTime.parse(date + "T" + time);
            }
        }

        //Pattern 3 - Matching format: "Fri Feb 15 22:38:49 PST 2019"
        {
            String re1 = "((?:Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Tues|Thur|Thurs|Sun|Mon|Tue|Wed|Thu|Fri|Sat))";    // Day Of Week 1
            String re2 = "(\\s+)";    // White Space 1
            String re3 = "((?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Sept|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?))";    // Month 1
            String re4 = "(\\s+)";    // White Space 2
            String re5 = "((?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // Day 1
            String re6 = "(\\s+)";    // White Space 3
            //String re7 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // HourMinuteSec 1
            String re7 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re8 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
            String re9 = "(\\s+)";    // White Space 4
            String re10 = "((?:[a-z][a-z]+))";    // TimeZone like PST
            String re11 = "(\\s+)";    // White Space 5
            String re12 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";    // Year 1

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11 + re12, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String monthNumber = findMonthNumber(m.group(3));
                String date = m.group(12) + "-" + monthNumber + "-" + m.group(5);
                String time;
                if (ignoreSeconds)
                    time = m.group(7);
                else
                    time = m.group(7) + m.group(8);

                return LocalDateTime.parse(date + "T" + time);
            }
        }

        //Pattern 4 - Matching format: "May 23, 2019 11:25:43"
        {
            String re1 = "((?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Sept|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?))";    // Month 1
            String re2 = "(\\s+)";    // White Space 1
            String re3 = "((?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // Day 1
            String re4 = "(,)";    // Any Single Character 1
            String re5 = "(\\s+)";    // White Space 2
            String re6 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";    // Year 1
            String re7 = "(\\s+)";    // White Space 3
            //String re8 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // HourMinuteSec 1
            String re8 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re9 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String monthNumber = findMonthNumber(m.group(1));
                System.out.println(monthNumber);
                String date = m.group(6) + "-" + monthNumber + "-" + m.group(3);
                String time;
                if (ignoreSeconds)
                    time = m.group(8);
                else
                    time = m.group(8) + m.group(9);

                return LocalDateTime.parse(date + "T" + time);
            }
        }

        //Pattern 5 - Matching format: "2012-05-23 11:25:43"
        {
            String re1 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))[-:\\/.](?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
            String re2 = "(\\s+)";    // White Space 1
            //String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // HourMinuteSec 1
            String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re4 = "((?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String date = m.group(1);
                String time;
                if (ignoreSeconds)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                return LocalDateTime.parse((date + "T" + time));
            }
        }

        //Pattern 6 -Matching format: "2019-12-31T00:18:45.805263"
        {
            String re1 = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))[-:\\/.](?:[0]?[1-9]|[1][012])[-:\\/.](?:(?:[0-2]?\\d{1})|(?:[3][01]{1})))(?![\\d])";    // YYYYMMDD 1
            String re2 = "([a-z])";    // Any Single Word Character (Not Whitespace) 1
            //String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";    // old one HourMinuteSec 1
            String re3 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9]))";    // HourMinute
            String re4 = "((?::[0-9][0-9][0-9][0-9][0-9][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
            // String re5 = "(Z)";    // Any Single Word Character (Not Whitespace) 2

            Pattern p = Pattern.compile(re1 + re2 + re3 + re4, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(dateTime);
            if (m.find()) {
                String date = m.group(1);
                String time;
                if (ignoreSeconds)
                    time = m.group(3);
                else
                    time = m.group(3) + m.group(4);

                return LocalDateTime.parse(date + "T" + time);
            }
        }
        return null;
    }//method end


    //find Month Number
    private String findMonthNumber(String monthName) {
        String monthNumber = "";
        switch (monthName) {
            case "Jan":
            case "January": {
                monthNumber = "01";
                break;
            }
            case "Feb":
            case "February": {
                monthNumber = "02";
                break;
            }
            case "March":
            case "Mar": {
                monthNumber = "03";
                break;
            }
            case "April":
            case "Apr": {
                monthNumber = "04";
                break;
            }
            case "May": {
                monthNumber = "05";
                break;
            }
            case "June":
            case "Jun": {
                monthNumber = "06";
                break;
            }
            case "July":
            case "Jul": {
                monthNumber = "07";
                break;
            }
            case "August":
            case "Aug": {
                monthNumber = "08";
                break;
            }
            case "September":
            case "Sep": {
                monthNumber = "09";
                break;
            }
            case "October":
            case "Oct": {
                monthNumber = "10";
                break;
            }
            case "November":
            case "Nov": {
                monthNumber = "11";
                break;
            }
            case "December":
            case "Dec": {
                monthNumber = "12";
                break;
            }
        }
        return monthNumber;
    }//switch statement

}
