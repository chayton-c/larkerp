package com.yingda.lkj.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author hood  2020/6/24
 */
public class MachineCodeUtil {
    public static String getMachineCode() throws Exception {
        return getMACAddress() + "-" + getIdentifier();
    }

    public static String getMACAddress() throws Exception {
        if (isLinux()) {
            return getMACAddressByLinux();
        } else {
            return getMACAddressByWindows();
        }
    }

    public static String getIdentifier() throws Exception {
        if (isLinux()) {
            return getIdentifierByLinux();
        } else {
            return getIdentifierByWindows();
        }
    }

    private static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private static String getMACAddressByLinux() throws Exception {
        String[] cmd = {"ifconfig"};

        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();

        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);

        String str1 = sb.toString();
        String str2 = str1.split("ether")[1].trim();
        String result = str2.split("txqueuelen")[0].trim();
        br.close();

        return result;
    }

    private static String getIdentifierByLinux() throws Exception {
        String[] cmd = {"fdisk", "-l"};

        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();

        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);

        String str1 = sb.toString();
        String str2 = str1.split("identifier:")[1].trim();
        String result = str2.split("Device Boot")[0].trim();
        br.close();

        return result;
    }

    private static String getMACAddressByWindows() throws Exception {
        String result = "";
        Process process = Runtime.getRuntime().exec("ipconfig /all");
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));

        String line;
        int index;
        while ((line = br.readLine()) != null) {
            index = line.toLowerCase().indexOf("物理地址");
            if (index >= 0) { // 找到了
                index = line.indexOf(":");
                if (index >= 0)
                    result = line.substring(index + 1).trim();
                break;
            }
        }
        br.close();
        return result;
    }

    private static String getIdentifierByWindows() throws Exception {
        String result = "";
        Process process = Runtime.getRuntime().exec("cmd /c dir C:");
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));

        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains("卷的序列号是 ")) {
                result = line.substring(line.indexOf("卷的序列号是 ") + "卷的序列号是 ".length());
                break;
            }
        }
        br.close();
        return result;
    }

    public static void main(String[] a) throws Exception {
        // 判断是Linux还是Windows
        if (isLinux()) {
            // Linux操作系统
            String macAddress = getMACAddressByLinux();
            System.out.println("Linux macAddress: " + macAddress);
            String Identifier = getIdentifierByLinux();
            System.out.println("Linux Identifier: " + Identifier);
        } else {
            // Windows操作系统
            String macAddress = getMACAddressByWindows();
            System.out.println("Windows macAddress: " + macAddress);
            String Identifier = getIdentifierByWindows();
            System.out.println("Windows Identifier: " + Identifier);
        }
    }
}
