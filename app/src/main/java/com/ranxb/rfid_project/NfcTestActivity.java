package com.ranxb.rfid_project;


import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class NfcTestActivity extends AppCompatActivity implements View.OnClickListener {
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    /**
     * 密码
     */
    private EditText password_text;
    /**
     * 读取的扇区
     */
    private EditText block_text;
    /**
     * 读取的块
     */
    private EditText piece_text;
    /**
     * 写的扇区
     */
    private EditText write_block_text;
    /**
     * 写的块
     */
    private EditText write_piece_text;
    /**
     * 写的内容
     */
    private EditText write_text;
    /**
     * 改密码的扇区
     */
    private EditText change_block_text;
    /**
     * 新密码
     */
    private EditText new_password_text;
    /**
     * 重复新密码
     */
    private EditText repeat_new_password_text;

    private TextView ReadData;
    private LinearLayout read_data;
    private LinearLayout write;
    private LinearLayout changePassword;
    private int flag = 0;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                toToast(msg.getData().getString("data"));
            } else if (msg.what == 2) {
                ReadData.setText(msg.getData().getString("data"));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        init();
        initData();
    }

    /**
     * 初始化控件
     */
    private void init() {
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        password_text = (EditText) findViewById(R.id.password_text);
        block_text = (EditText) findViewById(R.id.block_text);
        piece_text = (EditText) findViewById(R.id.piece_text);
        write_block_text = (EditText) findViewById(R.id.write_block_text);
        write_piece_text = (EditText) findViewById(R.id.write_piece_text);
        write_text = (EditText) findViewById(R.id.write_text);
        change_block_text = (EditText) findViewById(R.id.change_block_text);
        new_password_text = (EditText) findViewById(R.id.new_password_text);
        repeat_new_password_text = (EditText) findViewById(R.id.repeat_new_password_text);
        read_data = (LinearLayout) findViewById(R.id.read_data);
        write = (LinearLayout) findViewById(R.id.write);
        changePassword = (LinearLayout) findViewById(R.id.changePassword);
        ReadData = (TextView) findViewById(R.id.data);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
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

    /**
     * 每次刷NFC都会进这个方法
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ReadData.setText("");
        switch (flag) {
            //读取数据
            case 0:
                readAllData(intent);
                break;
            //读取特定数据
            case 1:
                readData(intent);
                break;
            //写入数据
            case 2:
                writeData(intent);
                break;
            //修改密码
            case 3:
                changePass(intent);
                break;
            default:
        }
    }

    /**
     * 写入数据
     */
    private void writeData(Intent intent) {
        String password = password_text.getText().toString().trim();
        String data = write_text.getText().toString().trim();
        if (write_block_text.getText().toString().trim().equals("")) {
            return;
        }
        if (write_piece_text.getText().toString().trim().equals("")) {
            return;
        }
        int block = Integer.parseInt(write_block_text.getText().toString().trim());
        int piece = Integer.parseInt(write_piece_text.getText().toString().trim());
        NFCWriteHelper.getInstence(intent)
                .setReadPassword(password)
                .writeData(data, block, piece, new NFCWriteHelper.NFCCallback() {
                    @Override
                    public void isSusses(boolean flag) {
                        Bundle bundle = new Bundle();
                        if (flag) {
                            bundle.putString("data", "写入成功");
                        } else {
                            bundle.putString("data", "写入失败");
                        }
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                });
    }

    /**
     * 修改密码
     *
     * @param intent
     */
    private void changePass(Intent intent) {
        String password = password_text.getText().toString().trim();
        String pass = new_password_text.getText().toString().trim();
        String pass1 = repeat_new_password_text.getText().toString().trim();
        if (pass.equals("") || pass1.equals("")) {
            toToast("请输入密码");
            return;
        }
        if (!pass.equals(pass1)) {
            toToast("密码不一致");
            return;
        }
        if (change_block_text.getText().toString().trim().equals("")) {
            toToast("选择扇区");
            return;
        }
        int block = Integer.parseInt(change_block_text.getText().toString().trim());
        NFCWriteHelper.getInstence(intent)
                .setReadPassword(password)
                .changePasword(pass1, block, new NFCWriteHelper.NFCCallback() {
                    @Override
                    public void isSusses(boolean flag) {
                        Bundle bundle = new Bundle();
                        if (flag) {
                            bundle.putString("data", "修改成功");
                        } else {
                            bundle.putString("data", "修改失败");
                        }
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                });
    }

    /**
     * 吐司
     *
     * @param str
     */
    private void toToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    /**
     * 读取指定数据
     */
    private void readData(Intent intent) {
        String password = password_text.getText().toString().trim();
        if (block_text.getText().toString().trim().equals("")) {
            return;
        }
        if (piece_text.getText().toString().trim().equals("")) {
            return;
        }
        int block = Integer.parseInt(block_text.getText().toString().trim());
        int piece = Integer.parseInt(piece_text.getText().toString().trim());
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
     * 读取全部数据
     */
    private void readAllData(Intent intent) {
        String password = password_text.getText().toString().trim();
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
     * 点击监听
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                flag = 0;
                changeVisiable(1);
                break;
            case R.id.button2:
                changeVisiable(2);
                flag = 1;
                break;
            case R.id.button3:
                changeVisiable(3);
                flag = 2;
                break;
            case R.id.button4:
                changeVisiable(4);
                flag = 3;
                break;
            default:
        }
    }

    /**
     * 改变界面状态
     *
     * @param i
     */
    private void changeVisiable(int i) {
        switch (i) {
            case 1:
                button1.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
                button2.setTextColor(getResources().getColor(R.color.shadow, null));
                button3.setTextColor(getResources().getColor(R.color.shadow, null));
                button4.setTextColor(getResources().getColor(R.color.shadow, null));
                read_data.setVisibility(View.GONE);
                write.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                break;
            case 2:
                button1.setTextColor(getResources().getColor(R.color.shadow, null));
                button2.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
                button3.setTextColor(getResources().getColor(R.color.shadow, null));
                button4.setTextColor(getResources().getColor(R.color.shadow, null));
                read_data.setVisibility(View.VISIBLE);
                write.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                break;
            case 3:
                button1.setTextColor(getResources().getColor(R.color.shadow, null));
                button2.setTextColor(getResources().getColor(R.color.shadow, null));
                button3.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
                button4.setTextColor(getResources().getColor(R.color.shadow, null));
                read_data.setVisibility(View.GONE);
                write.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                break;
            case 4:
                button1.setTextColor(getResources().getColor(R.color.shadow, null));
                button2.setTextColor(getResources().getColor(R.color.shadow, null));
                button3.setTextColor(getResources().getColor(R.color.shadow, null));
                button4.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
                read_data.setVisibility(View.GONE);
                write.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
                break;
            default:
        }
    }
}
