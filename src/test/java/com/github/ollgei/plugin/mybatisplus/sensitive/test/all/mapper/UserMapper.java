package com.github.ollgei.plugin.mybatisplus.sensitive.test.all.mapper;

import java.util.List;

import com.github.ollgei.plugin.mybatisplus.sensitive.test.all.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    int insert(UserDTO userDTO);

    int insert2(@Param("userName") String userName, @Param("idcard") String idcard);
    List<UserDTO> findAll();
    List<UserDTO> findAll2();
    UserDTO findOne(UserDTO userDTO);
    /**
     * 测试 通过加密字段查询的情况
     * @param userDTO ;
     * @return ;
     */
    List<UserDTO> findByCondition(UserDTO userDTO);

    /**
     * 测试更新时带条件字段，update的字段里有需要加密的字段，where条件里也有需要加密的
     * @param userDTO userDto
     * @return return
     */
    int updateByCondition(UserDTO userDTO);
}
