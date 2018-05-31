package cn.hitftcl.wearablepc.Login;

import android.util.Log;

import org.litepal.crud.DataSupport;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import  cn.hitftcl.wearablepc.Model.UserIPInfo;

/**
 * 加密工具类
 * Created by hzf on 2017/7/29.
 */

public class EncryptionUtil {
    private static final String TAG = EncryptionUtil.class.getSimpleName();

    /**
     * 验证身份
     * @param username
     * @param password
     * return 0：验证成功  1：用户名或密码不能为空  2：用户名或密码错误
     */
    public static int validateIdentity(String username, String password){
        if(username.isEmpty() || password.isEmpty())
            return 1;
        String passwordMd5 = md5(password);
        UserIPInfo userIPInfo = DataSupport.where("username = ?", username).findFirst(UserIPInfo.class);
        if(userIPInfo == null)
            return 2;
        if(userIPInfo.getPassword().equals(passwordMd5))
            return 0;
        else
            return 2;
    }

    /**
     * 用户注册函数（该版仅仅保存至本地数据库）
     * @param username
     * @param password
     * @return 0：成功  1：用户名或密码不能为空  2：用户名已存在  3：注册失败请重试
     */
    public static int register(String username, String password){
        if(username.isEmpty() || password.isEmpty())
            return 1;
        String passwordMd5 = md5(password);  //将明文加密
        UserIPInfo userIPInfo = DataSupport.where("username = ?", username).findFirst(UserIPInfo.class);  //查找该用户是否存在
        if(userIPInfo != null)
            return 2;
        //创建账号
        UserIPInfo addUser = new UserIPInfo(username, passwordMd5);
        if(addUser.save())
            return 0;   //注册成功
        return 3;
    }

    //md5加密
    public static String md5(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException",e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}
