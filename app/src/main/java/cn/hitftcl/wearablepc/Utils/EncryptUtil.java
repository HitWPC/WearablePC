package cn.hitftcl.wearablepc.Utils;

/**
 * Created by Administrator on 2018/11/26.
 */

import android.util.Base64;



import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


/**
 * 加密/解密工具
 * @author ershuai
 * @date 2017年4月18日 上午11:27:36
 */
public class EncryptUtil {

    public static final String PASSWORD_ENC_SECRET = "mythmayor";

    /**
     * 加密
     * @param clearText
     * @return
     */
    public static String encryptPassword(String clearText) {
        try {
            DESKeySpec keySpec = new DESKeySpec(
                    PASSWORD_ENC_SECRET.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            String encrypedPwd = Base64.encodeToString(cipher.doFinal(clearText
                    .getBytes("UTF-8")), Base64.DEFAULT);
            return encrypedPwd;
        } catch (Exception e) {
        }
        return clearText;
    }

    /**
     * 解密
     * @param encryptedPwd
     * @return
     */
    public static String decryptPassword(String encryptedPwd) {
        try {
            DESKeySpec keySpec = new DESKeySpec(PASSWORD_ENC_SECRET.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] encryptedWithoutB64 = Base64.decode(encryptedPwd, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainTextPwdBytes = cipher.doFinal(encryptedWithoutB64);
            return new String(plainTextPwdBytes);
        } catch (Exception e) {
        }
        return encryptedPwd;
    }

}
