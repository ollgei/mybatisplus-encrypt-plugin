package com.github.ollgei.plugin.mybatisplus.sensitive.interceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.chenhaiyang.plugin.mybatis.sensitive.Encrypt;
import com.chenhaiyang.plugin.mybatis.sensitive.annotation.EncryptField;
import com.chenhaiyang.plugin.mybatis.sensitive.annotation.SensitiveEncryptEnabled;
import com.chenhaiyang.plugin.mybatis.sensitive.annotation.SensitiveField;
import com.chenhaiyang.plugin.mybatis.sensitive.annotation.SensitiveJSONField;
import com.chenhaiyang.plugin.mybatis.sensitive.annotation.SensitiveJSONFieldKey;
import com.chenhaiyang.plugin.mybatis.sensitive.type.SensitiveType;
import com.chenhaiyang.plugin.mybatis.sensitive.type.SensitiveTypeRegisty;
import com.chenhaiyang.plugin.mybatis.sensitive.utils.JsonUtils;
import com.chenhaiyang.plugin.mybatis.sensitive.utils.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

/**
 * 拦截写请求的插件。插件生效仅支持预编译的sql
 * @author ;
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
})
@Slf4j
public class SensitiveAndEncryptWriteInterceptor implements Interceptor {

    private static final String MAPPEDSTATEMENT="delegate.mappedStatement";
    private static final String BOUND_SQL="delegate.boundSql";

    private Encrypt encrypt;

    public SensitiveAndEncryptWriteInterceptor(Encrypt encrypt) {
        Objects.requireNonNull(encrypt,"encrypt should not be null!");
        this.encrypt = encrypt;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement)metaObject.getValue(MAPPEDSTATEMENT);
        SqlCommandType commandType = mappedStatement.getSqlCommandType();

        BoundSql boundSql = (BoundSql)metaObject.getValue(BOUND_SQL);
        Object params = boundSql.getParameterObject();
        String prefix = "";
        if(params instanceof Map){
            Map map = (Map) params;
            if (map.containsKey("et")) {
                params = map.get("et");
                handleParameters(mappedStatement, boundSql, params, commandType);
            } if (map.containsKey("list")) {
                List paramList = (List) map.get("list");
                for (Object p : paramList) {
                    handleParameters(mappedStatement, boundSql, p, commandType);
                }
            } else {
                return invocation.proceed();
            }
        }
        handleParameters(mappedStatement, boundSql, params, commandType);
        return invocation.proceed();
    }

    private void handleParameters(MappedStatement mappedStatement, BoundSql boundSql,Object param,SqlCommandType commandType) throws Exception {
        SensitiveEncryptEnabled sensitiveEncryptEnabled = param != null ? param.getClass().getAnnotation(SensitiveEncryptEnabled.class) : null;
        if(sensitiveEncryptEnabled != null && sensitiveEncryptEnabled.value()){
            handleParameters(mappedStatement.getConfiguration(), boundSql,param,commandType);
        }
    }

    private void handleParameters(Configuration configuration, BoundSql boundSql,Object param,SqlCommandType commandType) throws Exception {

        MetaObject metaObject = configuration.newMetaObject(param);

        for (Field field : param.getClass().getDeclaredFields()) {

            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Object value = metaObject.getValue(field.getName());
            Object newValue = value;
            if(value instanceof CharSequence){
                newValue = handleEncryptField(field,newValue);
                if(isWriteCommand(commandType) && !SensitiveTypeRegisty.alreadyBeSentisived(newValue)) {
                    newValue = handleSensitiveField(field, newValue);
                    newValue = handleSensitiveJSONField(field, newValue);
                }
            }
            if(value!=null && newValue!=null && !value.equals(newValue)) {
                String name = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                Method setter = param.getClass().getMethod("set" + name, field.getType());
                if (setter != null) {
                    setter.invoke(param, newValue);
                }
            }

        }
    }

    private boolean isWriteCommand(SqlCommandType commandType) {
        return SqlCommandType.UPDATE.equals(commandType) || SqlCommandType.INSERT.equals(commandType);
    }

    private Object handleEncryptField(Field field, Object value) {

        EncryptField encryptField = field.getAnnotation(EncryptField.class);
        Object newValue = value;
        if (encryptField != null && value != null) {
            newValue = encrypt.encrypt(value.toString());
        }
        return newValue;
    }

    private Object handleSensitiveField(Field field, Object value) {
        SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
        Object newValue = value;
        if (sensitiveField != null && value != null) {
            newValue = SensitiveTypeRegisty.get(sensitiveField.value()).handle(value);
        }
        return newValue;
    }
    private Object handleSensitiveJSONField(Field field, Object value) {
        SensitiveJSONField sensitiveJSONField = field.getAnnotation(SensitiveJSONField.class);
        Object newValue = value;
        if (sensitiveJSONField != null && value != null) {
            newValue = processJsonField(newValue,sensitiveJSONField);
        }
        return newValue;
    }

    /**
     * 在json中进行脱敏
     * @param newValue new
     * @param sensitiveJSONField 脱敏的字段
     * @return json
     */
    private Object processJsonField(Object newValue,SensitiveJSONField sensitiveJSONField) {

        try{
            Map<String,Object> map = JsonUtils.parseToObjectMap(newValue.toString());
            SensitiveJSONFieldKey[] keys =sensitiveJSONField.sensitivelist();
            for(SensitiveJSONFieldKey jsonFieldKey :keys){
                String key = jsonFieldKey.key();
                SensitiveType sensitiveType = jsonFieldKey.type();
                Object oldData = map.get(key);
                if(oldData!=null){
                    String newData = SensitiveTypeRegisty.get(sensitiveType).handle(oldData);
                    map.put(key,newData);
                }
            }
            return JsonUtils.parseMaptoJSONString(map);
        }catch (Throwable e){
            //失败以后返回默认值
            log.error("脱敏json串时失败，cause : {}",e.getMessage(),e);
            return newValue;
        }
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {
        //do nothing
    }
}
