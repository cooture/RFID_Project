package com.ranxb.rfid_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage,showname,showphone,showprovice,showcity;
    private int flag = 0;
    private EditText edit_name;
    private EditText edit_phone;
    private EditText edit_city;

    private LinearLayout edit_layout,show_data;

    private Spinner spinner_provice;
    private ArrayAdapter<String> adapter = null;
    private static final String [] provice ={"Beijing","Shanghai","Shenzhen","Tianjin","Taiwan"};

    private String p_provice,p_city,p_name,p_phone;
    private String i_provice,i_city,i_name,i_phone;
    private String whole_data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        init();
        initData();
    }

    private void init() {
        mTextMessage = (TextView) findViewById(R.id.message);
        edit_name = findViewById(R.id.p_name);
        edit_phone = findViewById(R.id.p_phone);
        edit_city = findViewById(R.id.p_city);
        showname = findViewById(R.id.showname);
        showphone = findViewById(R.id.showphone);
        showprovice = findViewById(R.id.showprovice);
        showcity = findViewById(R.id.showcity);

        edit_layout = findViewById(R.id.edit_layout);
        show_data = findViewById(R.id.showdata);

        spinner_provice = findViewById(R.id.spinner_provice);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);


        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,provice);
        //设置下拉列表风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将适配器添加到spinner中去
        spinner_provice.setAdapter(adapter);
        spinner_provice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                try {
                    p_provice = ""+((TextView)arg1).getText();
                }catch (Exception e){
                    e.printStackTrace();
                }

//                toToast(""+((TextView)arg1).getText());
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });


    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
                    show_data.setVisibility(View.VISIBLE);
                    edit_layout.setVisibility(View.GONE);
                    mTextMessage.setText("");
                    mTextMessage.setVisibility(View.GONE);
                    flag = 0;


                    return true;
                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
                    edit_layout.setVisibility(View.VISIBLE);
                    mTextMessage.setText("");
                    mTextMessage.setVisibility(View.GONE);
                    show_data.setVisibility(View.GONE);
                    flag = 1;
                    return true;
                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
                    edit_layout.setVisibility(View.GONE);
                    mTextMessage.setVisibility(View.VISIBLE);
                    show_data.setVisibility(View.GONE);
                    flag = 2;
                    return true;
            }
            return false;
        }
    };


    private void toToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                toToast(msg.getData().getString("data"));
            } else if (msg.what == 2) {
                mTextMessage.setText(msg.getData().getString("data").trim());
            }else if (msg.what == 3){
                i_provice = msg.getData().getString("provice");
                i_name = msg.getData().getString("name");
                i_city = msg.getData().getString("city");
                i_phone = msg.getData().getString("phone");
                showname.setText(i_name);
                showphone.setText(i_phone);
                showprovice.setText(i_provice);
                showcity.setText(i_city);
//                mTextMessage.setText("info: \n"+i_provice+"\n"+i_city+"\n"+i_name+"\n"+i_phone);
            }
        }
    };




    public static String decode(String bytes) {
        String hexString = "0123456789abcdef";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
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

        NfcReadHelper.getInstence(intent)
                .setPassword(password)
                .getAllData(new NfcReadHelper.NFCCallback() {
                    @Override
                    public void callBack(Map<String, List<String>> data) {
                        Bundle bundle = new Bundle();
                        String m = "";
                        for (String key : data.keySet()) {
                            List list = data.get(key);
                            for (int i = 0; i < list.size(); i++) {
//                                String str = "" + list.get(i);
////                                toToast(NfcReadHelper.byteToString(list.get(i)));
//                                m = m + str+"#";
                                if (key.equals("5") && i == 1){
                                    bundle.putString("provice",decode(""+list.get(i)));
                                }else if (key.equals("5") && i == 2){
                                    bundle.putString("city",decode(""+list.get(i)));
                                }else if (key.equals("6") && i == 1){
                                    bundle.putString("name",decode(""+list.get(i)));
                                }else if (key.equals("6") && i == 2){
                                    bundle.putString("phone",decode(""+list.get(i)));
                                }

                            }
                        }
//                        bundle.putString("data",m);
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 3;
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
     * 写入数据
     */
    private void writeData(Intent intent, int block , int piece ,String data) {

        String password = null;


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
        switch (flag) {
            //读取数据
            case 0:
                toToast("read");
                readData(intent);
                break;
            case 1:
                p_name = edit_name.getText().toString().trim();
                p_city = edit_city.getText().toString().trim();;
                p_phone = edit_phone.getText().toString().trim();;
                writeData(intent,5,1,p_provice);
                writeData(intent,5,2,p_city);
                writeData(intent,6,1,p_name);
                writeData(intent,6,2,p_phone);
                break;
            case 2:
                readAllData(intent);
                break;


            default:
        }
    }

}
