package com.ranxb.rfid_project;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };


    private void toToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                toToast(msg.getData().getString("data"));
            } else if (msg.what == 2) {
                mTextMessage.setText(msg.getData().getString("data"));
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        initData();
    }


    /**
     * 读取全部数据
     */
    private void readAllData(Intent intent) {
//        String password = password_text.getText().toString().trim();
        String password = null;
        NfcReadHelper.getInstence(intent)
                .setPassword(password)
                .getAllData(new NfcReadHelper.NFCCallback() {
                    @Override
                    public void callBack(Map<String, List<String>> data) {
                        String text = "";
                        for (String key : data.keySet()) {
                            List list = data.get(key);
                            for (int i = 0; i < list.size(); i++) {
                                String str = "第" + key + "扇区" + "第" + i + "块内容：\n" + list.get(i);
//                                toToast(NfcReadHelper.byteToString(list.get(i)));
                                text += str + "\n";
                            }
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("data", text);
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 2;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void callBack(String data) {

                    }

                    @Override
                    public void error() {
                        Bundle bundle = new Bundle();
                        bundle.putString("data", "读取失败");
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                });
    }

    /**
     * 读取指定数据
     */
    private void readData(Intent intent) {
        String password = null;
//        if (block_text.getText().toString().trim().equals("")) {
//            return;
//        }
//        if (piece_text.getText().toString().trim().equals("")) {
//            return;
//        }
        int block = 0;
        int piece = 0;


//        int block = Integer.parseInt(block_text.getText().toString().trim());
//        int piece = Integer.parseInt(piece_text.getText().toString().trim());
        NfcReadHelper.getInstence(intent)
                .setPassword(password)
                .getData(block, piece, new NfcReadHelper.NFCCallback() {
                    @Override
                    public void callBack(Map<String, List<String>> data) {
                    }

                    @Override
                    public void callBack(String data) {
                        Bundle bundle = new Bundle();
                        bundle.putString("data", data);
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 2;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void error() {
                        Bundle bundle = new Bundle();
                        bundle.putString("data", "读取失败");
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                });
    }


    /**
     * 写入数据
     */
//    private void writeData(Intent intent) {
//        String password = password_text.getText().toString().trim();
//        String data = write_text.getText().toString().trim();
//        if (write_block_text.getText().toString().trim().equals("")) {
//            return;
//        }
//        if (write_piece_text.getText().toString().trim().equals("")) {
//            return;
//        }
//        int block = Integer.parseInt(write_block_text.getText().toString().trim());
//        int piece = Integer.parseInt(write_piece_text.getText().toString().trim());
//        NFCWriteHelper.getInstence(intent)
//                .setReadPassword(password)
//                .writeData(data, block, piece, new NFCWriteHelper.NFCCallback() {
//                    @Override
//                    public void isSusses(boolean flag) {
//                        Bundle bundle = new Bundle();
//                        if (flag) {
//                            bundle.putString("data", "写入成功");
//                        } else {
//                            bundle.putString("data", "写入失败");
//                        }
//                        Message message = new Message();
//                        message.setData(bundle);
//                        message.what = 1;
//                        handler.sendMessage(message);
//                    }
//                });
//    }



    private void initData() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (null == adapter) {
            Toast.makeText(this, "不支持NFC功能", Toast.LENGTH_SHORT).show();
        } else if (!adapter.isEnabled()) {
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            // 根据包名打开对应的设置界面
            startActivity(intent);
        }else {
            Toast.makeText(this, "已初始化完毕", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mTextMessage.setText("");
        switch (0) {
            //读取数据
            case 0:
                toToast("readdata");
                readAllData(intent);
                break;
            default:
        }
    }

}
