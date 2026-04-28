package com.hstk.iot_warehouse;

import com.hstk.iot_warehouse.mapper.SysUserMapper;
import com.hstk.iot_warehouse.model.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class IotWarehouseApplicationTests {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    public void testSelect(){
        SysUser sysUser =sysUserMapper.selectByUsername("admin");
        System.out.println(sysUser);
    }

   //@Test
   public void testQuery(){
       SysUser sysUser=new SysUser();
       sysUser = sysUserMapper.selectById(1L);
       System.out.println(sysUser);
   }

   // @Test
    public void testUpdate(){
        SysUser sysUser=new SysUser();
        sysUser.setUserId(10L);
        sysUser.setUsername("test2");
        sysUser.setPassword("123456");
        sysUser.setNickname("测试2");
        sysUser.setStatus(1);
        sysUserMapper.update(sysUser);
    }

    //@Test
    public void testListAll() {
        List<SysUser> listusers = sysUserMapper.selectAll();
        listusers.stream().forEach(sysUser -> {
            System.out.println(sysUser);
        });
    }

    //@Test
    public void testInser(){
        SysUser sysUser=new SysUser();
        sysUser.setUsername("test1");
        sysUser.setPassword("123456");
        sysUser.setNickname("测试1");
        sysUser.setStatus(1);
        sysUserMapper.insert(sysUser);
    }


}
