package com.serialport;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;
/*串口类
作用：JNI的调用，用来加载.so文件,获取串口输入输出流*/
//通过打开JNI的调用，打开串口。获取串口通信中的输入输出流，通过操作IO流，达到能够利用串口接收数据和发送数据的目的
public class SerialPort {
    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */

    /** 串口文件描述符，禁止删除或重命名，因为native层关闭串口时需要使用 */
    private FileDescriptor mFd;
    /** 输入流，用于接收串口数据 */
    private FileInputStream mFileInputStream;
    /** 输出流，用于发送串口数据 */
    private FileOutputStream mFileOutputStream;

    /**
     * 构造函数
     *
     * @param device
     *            串口名
     * @param baudrate
     *            波特率
     * @param flags
     *            操作标识
     * @throws SecurityException
     *             安全异常，当串口文件不可读写时触发
     * @throws IOException
     *             IO异常，开启串口失败时触发
     */
    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

        /* 检测设备管理权限，即文件的权限属性 */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 777 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }
        //开启串口，传入物理地址，波特率，flag值
        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        // 输入流，也就是获取从单片机或者传感器，通过串口传入到Android主板的IO数据（使用的时候，执行Read方法）
        //将外部存储的数据读取到内存里
        mFileInputStream = new FileInputStream(mFd);
        //输出流，Android将需要传输的数据发送到单片机或者传感器（使用的时候，执行Write方法）
        //将内存的数据写到外部存储
        mFileOutputStream = new FileOutputStream(mFd);
    }

    /**
     * 获取输入流
     *
     * @return 串口输入流
     */
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    /**
     * 获取输出流
     *
     * @return 串口输出流
     */
    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    /**
     * 原生函数，开启串口虚拟文件
     *
     * @param path
     *            串口虚拟文件路径
     * @param baudrate
     *            波特率
     * @param flags
     *            操作标识
     * @return
     */
    //JNI调用，开启串口
    private native static FileDescriptor open(String path, int baudrate, int flags);

    /**
     * 原生函数，关闭串口虚拟文件
     */
    //关闭串口
    public native void close();

    static {
        //加载库文件，so文件
        System.loadLibrary("serial_port");
    }
}
