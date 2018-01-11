package com.bob.wd;

import com.bob.wd.consumer.*;
import com.bob.wd.provider.DubboProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangxiang on 17/11/30.
 */
public class How2UsingExample {

    private Map<String, Object> params = new HashMap<>();
    private DubboConsumerConfig dubboConsumerConfig = new DubboConsumerConfig("bob.dubbo.cp.test", "zookeeper", "zk.wd.com:2181", 2500);
    private DubboConsumer dubboConsumer = new DubboConsumer(dubboConsumerConfig);

    static {
        System.out.println("             ------------------------------------             ");
        System.out.println("业务方可以参考此代码进行添加-所有的dubbo服务均保存在RuleHolder的map中");
        System.out.println("支持简单类型的入参或者入参只有一个DTO-暂时不支持DTO外加简单类型同时入参");
        System.out.println("             ------------------------------------             ");

        UniqueServiceDef temp = new UniqueServiceDef();
        temp.setInterfaceName("com.weidai.sso.client.api.MobileLoginFacade");
        temp.setMethod("getKey");
        temp.setInputEnum("username");
        temp.setParamType("String");
        temp.setVersion("2.0");
        RuleHolder.add(temp);

        System.out.println(" using " + DubboProvider.class.getSimpleName() + " to register a service ");
    }

    public void testParamIsDto() {

        UniqueServiceDef uniqueServiceDef = RuleHolder.find("com.weidai.sso.client.api.SalesTeamFacade-addWithGroup-2.0-g");
        params.clear();
        if (uniqueServiceDef != null) {
            params.put("teamName", "abcd");
            params.put("fromGroupId", 12l);
            params.put("fromGroupName", "abcd");
            Object obj = dubboConsumer.invoke(uniqueServiceDef, params);
            System.out.println(obj);
        }
    }

    public void testParamIsNotDtoAndMoreParam() {

        UniqueServiceDef uniqueServiceDef = RuleHolder.find("com.weidai.sso.client.api.SalesTeamFacade-updateNameAndLeader-2.0-g");
        params.clear();
        params.put("salesTeamId", 1l);
        params.put("newTeamName", "abcd");
        params.put("ssoUserIdOfLeader", 2l);

        Object obj = dubboConsumer.invoke(uniqueServiceDef, params);
        System.out.println(obj);
    }

    public void testParamIsNotDtoButOneParam() {
        UniqueServiceDef uniqueServiceDef = RuleHolder.find("com.weidai.sso.client.api.UserFacade-getUserById-2.0-g");
        params.clear();
        params.put("userId", 23l);
        Object obj = dubboConsumer.invoke(uniqueServiceDef, params);
        System.out.println(obj);
    }

    public void testParamIsNotDtoButIsOneArray() {

        UniqueServiceDef uniqueServiceDef = RuleHolder.find("com.weidai.sso.client.api.UserFacade-getUsersByIds-2.0-g");
        params.clear();
        params.put("userIds", new Long[]{1l, 2l, 41l});
        Object obj = dubboConsumer.invoke(uniqueServiceDef, params);
        System.out.println(obj);
    }

    public void testParamIsNotDtoOthers() {
        UniqueServiceDef uniqueServiceDef = RuleHolder.find("com.weidai.sso.client.api.SalesmanFacade-configWithTeam-2.0-g");
        params.clear();
        params.put("teamId", 12l);
        Set<Long> s = new HashSet<>();
        s.add(12l);
        params.put("ssoUserIds", s);

        Object obj = dubboConsumer.invoke(uniqueServiceDef, params);
        System.out.println(obj);
    }

    public void testTempAdd() {
        UniqueServiceDef uniqueServiceDef = RuleHolder.find("com.weidai.sso.client.api.MobileLoginFacade-getKey-2.0-g");
        params.clear();
        params.put("username", "abcd");
        Object obj = dubboConsumer.invoke(uniqueServiceDef, params);
        System.out.println(obj);
    }
}