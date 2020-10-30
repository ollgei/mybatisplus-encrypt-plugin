package com.github.ollgei.plugin.mybatisplus.sensitive.test.encrypt;

import java.security.NoSuchAlgorithmException;

import com.github.ollgei.plugin.mybatisplus.sensitive.encrypt.AesSupport;
import org.junit.Test;

public class AesEncryptTest {

    @Test
    public void test() throws NoSuchAlgorithmException {
        String key="1870577f29b17d6787782f35998c4a79";
        String src ="测试原文";
        AesSupport aesSupport = new AesSupport(key);
        String result = aesSupport.encrypt(src);
        System.out.println(result);
        String src2 = aesSupport.decrypt(result);
        System.out.println(src2);
    }
}
