# SerialPort_Android
安卓模拟器与PC串口通信
一个模拟串口通信的APP程序,具有打开串口，发送数据，关闭串口的简单功能

参考：https://blog.csdn.net/gd6321374/article/details/74779770

用到虚拟串口，串口助手

调试过程遇到的几个问题：

    1.AVD模拟的API应小于安卓版本5.0，否则可能出现权限问题（例如找不到system/bin/su），之前没注意权限问题，花了大量时间解决
    如何获取安卓模拟器的root权限,还遇到例如（cmd 执行adb root显示:adbd cannot run as root in production builds）的问题
    
    2.利用cmd命令 (SDK路径)emulator @模拟器名字 -qemu -serial COMX(电脑串口号)
  
    3.SerialPortTest ttyS2改成ttyS1，我用COM1表示安卓模拟器，如果用COM2应该要用ttyS2吧
