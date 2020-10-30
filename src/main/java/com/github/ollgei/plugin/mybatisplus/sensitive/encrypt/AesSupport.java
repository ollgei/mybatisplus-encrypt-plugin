package com.github.ollgei.plugin.mybatisplus.sensitive.encrypt;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.github.ollgei.plugin.mybatisplus.sensitive.type.SensitiveType;
import com.github.ollgei.plugin.mybatisplus.sensitive.type.SensitiveTypeHandler;
import com.github.ollgei.plugin.mybatisplus.sensitive.type.SensitiveTypeRegisty;
import com.github.ollgei.plugin.mybatisplus.sensitive.utils.Hex;
import com.github.ollgei.plugin.mybatisplus.sensitive.Encrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据脱敏用到的AES加解密类
 * @author chenhaiyang
 */
@Slf4j
public class AesSupport implements Encrypt {

    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    private String password;
    private SecretKeySpec secretKeySpec;
    private SensitiveTypeHandler sensitiveTypeHandler = SensitiveTypeRegisty.get(SensitiveType.DEFAUL);

    public AesSupport(String password) throws NoSuchAlgorithmException {

        if(StringUtils.isEmpty(password)){
            throw new IllegalArgumentException("password should not be null!");
        }

        this.password = password;
        this.secretKeySpec = getSecretKey(password);
    }

    @Override
    public String encrypt(String value){

        try{
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] content = value.getBytes("UTF-8");
            byte[] encryptData = cipher.doFinal(content);

            return Hex.bytesToHexString(encryptData);
        }catch (Exception e){
            log.error("AES加密时出现问题，密钥为：{}",sensitiveTypeHandler.handle(password));
            throw new IllegalStateException("AES加密时出现问题"+e.getMessage(),e);
        }
    }

    @Override
    public String decrypt(String value){
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            byte[] encryptData = Hex.hexStringToBytes(value);
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] content = cipher.doFinal(encryptData);
            return new String(content, "UTF-8");
        }catch (Exception e){
            log.error("AES解密时出现问题，密钥为:{},密文为：{}",sensitiveTypeHandler.handle(password),value);
            throw new IllegalStateException("AES解密时出现问题"+e.getMessage(),e);
        }

    }


    private static SecretKeySpec getSecretKey(final String password) throws NoSuchAlgorithmException{
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        //AES 要求密钥长度为 128
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(password.getBytes());
        kg.init(128, random);
        //生成一个密钥
        SecretKey secretKey = kg.generateKey();
        // 转换为AES专用密钥
        return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
    }
}
