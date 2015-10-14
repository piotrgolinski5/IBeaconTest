package pl.apg.ibeaconlibrary.utils;

public class StringUtils {

    public static String addStrings(Object... params) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object s : params) {
            if (s instanceof String[]) {
                String[] array = (String[]) s;
                for (String ss : array
                        ) {
                    stringBuilder.append(ss);
                }
            } else {
                stringBuilder.append(s);
            }
        }

        return stringBuilder.toString();

    }

}
