package com.serialportndk;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.io.File;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serialport.SerialPort;

public class SerialPortTest extends AppCompatActivity implements OnClickListener{

    //private static final String Tag = SerialPortTest.class.getSimpleName();
    private Button mSendButton,openButton,closeButton;
    private EditText mSendEdit;
    private TextView mReceiveEdit;
    private Handler mHandler;
    private SerialPort serialPort = null;
    private InputStream minputStream = null;
    private OutputStream moutputStream = null;
    private ReceiveThread mReceiveThread = null;
    private boolean isStart = false;//标记当前串口状态（true:打开,false:关闭）


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1){
                    mReceiveEdit.setText("");
                    mReceiveEdit.setText(new String((byte[]) msg.obj,0,msg.arg1));
                }
            }
        };
    }

    private void initView(){
        openButton = (Button)findViewById(R.id.open);
        openButton.setOnClickListener(this);

        closeButton=(Button)findViewById(R.id.close);
        closeButton.setOnClickListener(this);

        mSendButton = (Button) findViewById(R.id.send);
        mSendButton.setOnClickListener(this);

        mSendEdit = (EditText) findViewById(R.id.edit_send);
        mReceiveEdit = (TextView) findViewById(R.id.tv_receive);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open:
                try{
                    openSerialPort();
                }catch(SecurityException e){
                    e.printStackTrace();
                }
                break;
            case R.id.send:
                sendString(mSendEdit.getText().toString());
                break;
            case R.id.close:
                closeSerialPort();
                break;
        }
    }

    //将要发送的字符串转化成字节型放在输出管道里，然后write出去
    private void sendString(String str){
        if(moutputStream!=null){
            try {
                moutputStream.write(str.getBytes("GBK"));
                moutputStream.flush();
                Toast.makeText(getApplicationContext(),"send data success!",Toast.LENGTH_SHORT).show();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开串口，接收数据
     * 通过串口，接收单片机发送来的数据
     */
    public void openSerialPort() {
        System.out.println(isStart);
        Log.i("test", "打开串口");
        try {
            serialPort = new SerialPort(new File("/dev/ttyS1"), 9600, 0);
            //调用对象SerialPort方法，获取串口中"读和写"的数据流
            minputStream = serialPort.getInputStream();
            moutputStream = serialPort.getOutputStream();
            isStart = true;
            getSerialPort();
            Toast.makeText(getApplicationContext(),"open serialport success!",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(SecurityException e){
            e.printStackTrace();
        }

    }

    private void getSerialPort() {
        if (mReceiveThread == null) {
            mReceiveThread = new ReceiveThread();
        }
        mReceiveThread.start();
    }

    /**
     * 接收串口数据的线程
     */

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            //条件判断，只要条件为true，则一直执行这个线程
            while (isStart) {
                if (minputStream == null) {
                    return;
                }
                byte[] readData = new byte[64];
                try {
                    int size = minputStream.read(readData);
                    if (size > 0) {
                        onDataReceived(readData, size);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    protected void onDataReceived(byte[] readData, int size) {
        Message msg = new Message();
        msg.what = 1;
        msg.obj = readData;
        msg.arg1 = size;
        mHandler.sendMessage(msg);//主动发送消息
    }

    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * 关闭串口
     * 关闭串口中的输入输出流
     */
    public void closeSerialPort() {
        System.out.println(isStart);
        Log.i("test", "关闭串口");
        try {
            if (minputStream != null) {
                minputStream.close();
            }
            if (moutputStream != null) {
                moutputStream.close();
            }
            isStart = false;
            Toast.makeText(getApplicationContext(),"close serialport success!",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
