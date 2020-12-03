package com.yingda.lkj.utils;

import com.yingda.lkj.beans.entity.system.HqlVersion;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.service.base.BaseService;
import com.yingda.lkj.utils.encript.RSAEncrypt;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author hood  2020/4/30
 */
public class LicensingUtil {
    // rsa私钥用于解密
    private static final ResourceBundle bundle = ResourceBundle.getBundle("config");

    private static String projectName = bundle.getString("projectName"); // 项目名

    public static Json validate() {
        ClassPathResource certificate = new ClassPathResource("/static/uploadimg/akagi.cert");
        return verificationCertificate(certificate);
    }


    private static Json verificationCertificate(ClassPathResource certificate) {
        try {
            String certificateStr = new String(certificate.getInputStream().readAllBytes());
            return getMessage(certificateStr);
        } catch (IOException e) {
            return new Json(JsonMessage.LICENSING_ERROR, e);
        }
    }

    private static Json getMessage(String text) {
        try {
            String[] split = text.split("\n");
            String privateKey = split[0]; // 私钥
            String certificateMachineCode = RSAEncrypt.decrypt(split[1], privateKey); // 机器码
            String certificateProjectName = RSAEncrypt.decrypt(split[2], privateKey); // 项目名
            String timestampStr = RSAEncrypt.decrypt(split[3], privateKey); // 有效时间

            if (!projectName.equals(certificateProjectName))
                return new Json(JsonMessage.LICENSING_ERROR, "找不到对应项目的软件授权证书，请联系管理员");

            // 比较授权时间
            if (Long.parseLong(timestampStr) < System.currentTimeMillis())
                return new Json(JsonMessage.LICENSING_ERROR, "软件授权证书过期，请联系管理员");

            // 获取本机机器码
            String machineCode = getSystemCode();
            if (!certificateMachineCode.equals(machineCode))
                return new Json(JsonMessage.LICENSING_ERROR, "软件授权证书与机器码不一致");

            return new Json(JsonMessage.SUCCESS, Long.parseLong(timestampStr));
        } catch (Exception e) {
            return new Json(JsonMessage.LICENSING_ERROR, "读取软件授权证书异常");
        }
    }


    public static String getSystemCode() {
        BaseService<HqlVersion> hqlVersionBaseService = (BaseService<HqlVersion>) SpringContextUtil.getBean("baseService");
        try {
            List<HqlVersion> hqlVersions = hqlVersionBaseService.getAllObjects(HqlVersion.class);

            if (hqlVersions.isEmpty()) {
                HqlVersion hqlVersion = new HqlVersion(UUID.randomUUID().toString());
                hqlVersionBaseService.saveOrUpdate(hqlVersion);
                hqlVersions.add(hqlVersion);
            }

            HqlVersion hqlVersion = hqlVersions.get(0);
            return hqlVersion.getId() + "-" + MachineCodeUtil.getMachineCode();
        } catch (Exception e) {
            throw new RuntimeException("获取授权证书机器码时异常");
        }
    }
}
