package com.example.bletestv3;

import java.util.UUID;

/**
 * Created by darwinhu on 2017/10/31.
 */

@SuppressWarnings("WeakerAccess")
public class Common {
    public static String TAG = "Common";
    public static final String m_szRoot = "GTService";

    public static boolean DEBUG = false;

    // HM_10
    public static class HM10{
        public final int ID = 0;
        public static UUID SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
        public static UUID RX = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
        public static UUID TX = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    }

    // NORDIC
    public static class NORDIC{
        public final int ID = 1;
        public static UUID SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
        public static UUID RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
        public static UUID TX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    }

    public static UUID SERVICE_UUID = HM10.SERVICE;//UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static UUID RX_UUID = HM10.RX;//UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static UUID TX_UUID = HM10.TX;//UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

//    // Nordic
//    public static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
//    public static final UUID RX_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
//    public static final UUID TX_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

//    // HM_10
//    public static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
//    public static final UUID RX_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
//    public static final UUID TX_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    public static class _fancmd{
        public static final String OFF = "AA010100AC";
        public static final String LV1 = "AA";//"AA010103AF";
        public static final String LV2 = "BB";//"AA010102AE";
        public static final String LV3 = "CC";//"AA010101AD";
    }

    /*
    第1顆(開) : 0xAA 0x01 0x02 0x01 總和不含進位
    第1顆(關) : 0xAA 0x01 0x02 0x00 總和不含進位

    第2顆(開) : 0xAA 0x01 0x03 0x01 總和不含進位
    第2顆(關) : 0xAA 0x01 0x03 0x00 總和不含進位

    第3顆(開) : 0xAA 0x01 0x04 0x01 總和不含進位
    第3顆(關) : 0xAA 0x01 0x04 0x00 總和不含進位

     */
    public static class _lampcmd{
        public static final String lamp1_on     = "AA010201AE";
        public static final String lamp1_off    = "AA010200AD";
        public static final String lamp2_on     = "AA010301AF";
        public static final String lamp2_off    = "AA010300AE";
        public static final String lamp3_on     = "AA010401B0";
        public static final String lamp3_off    = "AA010400AF";
    }

    //************************************************************************************************
    //	METHOD FUNCTION
    //************************************************************************************************

    public static void SetBLE(int nID){
        if(nID == 0){
            Common.SERVICE_UUID = HM10.SERVICE;
            Common.RX_UUID = HM10.RX;
            Common.TX_UUID = HM10.TX;
        }
        else if (nID == 1){
            Common.SERVICE_UUID = NORDIC.SERVICE;
            Common.RX_UUID = NORDIC.RX;
            Common.TX_UUID = NORDIC.TX;
        }

    }

    public static byte[] hexStringToByteArray(String s) {

        // Fixme: 不夠完整!如果輸入了超過 Hex 可支援數值就會出錯
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }



}
