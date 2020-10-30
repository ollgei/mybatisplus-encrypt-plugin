package com.github.ollgei.plugin.mybatisplus.sensitive.type.handler;

import com.github.ollgei.plugin.mybatisplus.sensitive.type.SensitiveType;
import com.github.ollgei.plugin.mybatisplus.sensitive.type.SensitiveTypeHandler;

/**
 * 不脱敏
 * @author chenhaiyang
 */
public class NoneSensitiveHandler implements SensitiveTypeHandler {
    @Override
    public SensitiveType getSensitiveType() {
        return SensitiveType.NONE;
    }

    @Override
    public String handle(Object src) {
        if(src!=null){
            return src.toString();
        }
        return null;
    }
}
