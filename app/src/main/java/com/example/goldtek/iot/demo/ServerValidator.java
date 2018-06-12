package com.example.goldtek.iot.demo;

import java.util.regex.Pattern;

/**
 * Created by Terry on 2018/06/12.
 */
public class ServerValidator {
    private final static String ValidIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    private final static String ValidHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    public boolean isValidIPV4(String input) {
        return Pattern.compile(ValidIpAddressRegex).matcher(input).matches();
    }

    public boolean isValidHost(String input) {
        return Pattern.compile(ValidHostnameRegex).matcher(input).matches();
    }
}
