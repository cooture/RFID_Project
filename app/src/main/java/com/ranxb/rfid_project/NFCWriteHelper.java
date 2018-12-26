package com.ranxb.rfid_project;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

import java.io.IOException;

/**
 * @author zx
 * @date 2018/4/25 8:08
 * email 1058083107@qq.com
 * description
 */
public class NFCWriteHelper {

    private Tag tag;
    private NFCWriteHelper.NFCCallback callback;
    private static NFCWriteHelper helper;
    private byte[] bytes = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    private static int PASSWORD_LENTH = 6;

    public NFCWriteHelper(Intent intent) {
        this.tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }

    /**
     * 单例初始化
     *
     * @param intent
     * @return
     */
    public static NFCWriteHelper getInstence(Intent intent) {
        if (helper == null) {
            helper = new NFCWriteHelper(intent);
        }
        return helper;
    }

    /**
     * 设置NFC卡的读取密码
     *
     * @param str
     * @return
     */
    public NFCWriteHelper setReadPassword(String str) {
        if (null != str && (str.length() <= PASSWORD_LENTH)) {
            for (int i = 0; i < str.length(); i++) {
                bytes[i] = (byte) str.charAt(i);
            }
        }
        return helper;
    }

    /**
     * 写卡
     *
     * @param str      书写内容，16个字节
     * @param a        书写的扇区 (从0开始数)
     * @param b        书写的块(从0开始数)
     * @param callback 返回监听
     */
    public void writeData(String str, int a, int b, NFCWriteHelper.NFCCallback callback) {
        MifareClassic mfc = MifareClassic.get(tag);
        byte[] data = new byte[16];
        if (null != mfc) {
            try {
                //连接NFC
                mfc.connect();
                //获取扇区数量
                int count = mfc.getSectorCount();
                //如果传进来的扇区大了或者小了直接退出方法
                if (a > count - 1 || a < 0) {
                    callback.isSusses(false);
                    return;
                }
                //获取写的扇区的块的数量
                int bCount = mfc.getBlockCountInSector(a);
                //如果输入的块大了或者小了也是直接退出
                if (b > bCount - 1 || b < 0) {
                    callback.isSusses(false);
                    return;
                }
                //将字符转换为字节数组，这样其实并不好，无法转换汉字
                for (int i = 0; i < 16; i++) {
                    if (i < str.length()) {
                        data[i] = (byte) str.charAt(i);
                    } else {
                        data[i] = (byte) ' ';
                    }
                }
                //验证扇区密码
                boolean isOpen = mfc.authenticateSectorWithKeyA(a, bytes);
                if (isOpen) {
                    int bIndex = mfc.sectorToBlock(a);
                    //写卡
                    mfc.writeBlock(bIndex + b, data);
                }
                callback.isSusses(true);
            } catch (Exception e) {
                e.printStackTrace();
                callback.isSusses(false);
            } finally {
                try {
                    mfc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 修改密码
     *
     * @param password 书写密码，16个字节
     * @param a        书写的扇区
     * @param callback 返回监听
     */
    public void changePasword(String password, int a, final NFCWriteHelper.NFCCallback callback) {
        MifareClassic mfc = MifareClassic.get(tag);
        byte[] data = new byte[16];
        if (null != mfc) {
            try {
                mfc.connect();
                if (password.length() != PASSWORD_LENTH) {
                    callback.isSusses(false);
                    return;
                }
                int count = mfc.getSectorCount();
                if (a > count - 1 || a < 0) {
                    callback.isSusses(false);
                    return;
                }
                //将密码转换为keyA
                for (int i = 0; i < password.length(); i++) {
                    data[i] = (byte) password.charAt(i);
                }
                //将密码转换为KeyB
                for (int i = 0; i < password.length(); i++) {
                    data[i + password.length() + 4] = (byte) password.charAt(i);
                }
                //输入控制位
                data[password.length()] = (byte) 0xff;
                data[password.length() + 1] = (byte) 0x07;
                data[password.length() + 2] = (byte) 0x80;
                data[password.length() + 3] = (byte) 0x69;
                //验证密码
                boolean isOpen = mfc.authenticateSectorWithKeyA(a, bytes);
                if (isOpen) {
                    int bIndex = mfc.sectorToBlock(a);
                    int bCount = mfc.getBlockCountInSector(a);
                    //写到扇区的最后一个块
                    mfc.writeBlock(bIndex + bCount - 1, data);
                }
                callback.isSusses(true);
            } catch (Exception e) {
                e.printStackTrace();
                callback.isSusses(false);
            } finally {
                try {
                    mfc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 返回监听类
     */
    public interface NFCCallback {
        /**
         * 返回是否成功
         *
         * @param flag
         */
        void isSusses(boolean flag);
    }
}
