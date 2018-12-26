package com.ranxb.rfid_project;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zx
 * @date 2018/4/23 14:31
 * email 1058083107@qq.com
 * description nfc读取工具类
 */
public class NfcReadHelper {
    private Tag tag;
    private NFCCallback callback;
    private static NfcReadHelper helper;
    /**
     * 默认密码
     */
    private byte[] bytes = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

    public NfcReadHelper(Intent intent) {
        this.tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }

    /**
     * 单例初始化
     *
     * @param intent
     * @return
     */
    public static NfcReadHelper getInstence(Intent intent) {
        if (helper == null) {
            helper = new NfcReadHelper(intent);
        }
        return helper;
    }

    /**
     * 设置NFC卡的密码
     *
     * @param str
     * @return
     */
    public NfcReadHelper setPassword(String str) {
        if (null != str && (str.length() <= 6)) {
            for (int i = 0; i < str.length(); i++) {
                bytes[i] = (byte) str.charAt(i);
            }
        }
        return helper;
    }

    /**
     * 读取NFC卡的全部信息
     *
     * @param callback
     */
    public void getAllData(final NFCCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, List<String>> map = new HashMap<>();
                MifareClassic mfc = MifareClassic.get(tag);
                if (null != mfc) {
                    try {
                        //链接NFC
                        mfc.connect();
                        //获取扇区数量
                        int count = mfc.getSectorCount();
                        //用于判断时候有内容读取出来
                        boolean flag = false;
                        for (int i = 0; i < count; i++) {
                            List<String> list = new ArrayList<>();
                            //验证扇区密码，否则会报错（链接失败错误）
                            boolean isOpen = mfc.authenticateSectorWithKeyA(i, bytes);
                            if (isOpen) {
                                //获取扇区里面块的数量
                                int bCount = mfc.getBlockCountInSector(i);
                                //获取扇区第一个块对应芯片存储器的位置（我是这样理解的，因为第0扇区的这个值是4而不是0）
                                int bIndex = mfc.sectorToBlock(i);
                                //String data1 = "";
                                for (int j = 0; j < bCount; j++) {
                                    //读取数据
                                    byte[] data = mfc.readBlock(bIndex);
                                    bIndex++;
                                    list.add(byteToString(data));
                                }
                                flag = true;
                            }
                            map.put(i + "", list);
                        }
                        if (flag) {
                            callback.callBack(map);
                        } else {
                            callback.error();
                        }
                    } catch (Exception e) {
                        callback.error();
                        e.printStackTrace();
                    } finally {
                        try {
                            mfc.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 读取NFC卡的特定扇区信息
     *
     * @param a        扇区
     * @param b        块
     * @param callback
     */
    public void getData(final int a, final int b, final NFCCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, List<String>> map = new HashMap<>();
                MifareClassic mfc = MifareClassic.get(tag);
                if (null != mfc) {
                    try {
                        mfc.connect();
                        int count = mfc.getSectorCount();
                        if (a < 0 || a > count - 1) {
                            callback.error();
                            return;
                        }
                        int bCount = mfc.getBlockCountInSector(a);
                        if (b < 0 || b > bCount - 1) {
                            callback.error();
                            return;
                        }
                        boolean isOpen = mfc.authenticateSectorWithKeyA(a, bytes);
                        if (isOpen) {
                            int bIndex = mfc.sectorToBlock(a);
                            byte[] data = mfc.readBlock(bIndex + b);

                            callback.callBack(byteToString(data));
                        } else {
                            callback.error();
                        }
                    } catch (Exception e) {

                        System.out.print(e);
                        callback.error();
                        e.printStackTrace();

                    } finally {
                        try {
                            mfc.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 返回监听类
     */
    public interface NFCCallback {
        /**
         * 返回读取nfc卡的全部信息
         *
         * @param data 前面代表扇区 四个块的数据用#号隔开
         */
        void callBack(Map<String, List<String>> data);

        void callBack(String data);

        void error();
    }

    /**
     * 将byte数组转化为字符串
     *
     * @param src
     * @return
     */
    public static String byteToString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
}
