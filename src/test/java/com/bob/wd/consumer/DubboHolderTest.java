package com.bob.wd.consumer;

import com.bob.wd.How2UsingExample;
import javassist.*;
import org.junit.Test;

/**
 * Created by wangxiang on 17/11/30.
 */
public class DubboHolderTest {

    private How2UsingExample how2UsingExample = new How2UsingExample();

    @Test
    public void testJavaAssist() throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get("com.bob.wd.consumer.Hello");
        CtMethod m = cc.getDeclaredMethod("say");
        m.insertBefore("{ System.out.println(\"Hello.say():\"); }");
        Class c = cc.toClass();
        Hello h = (Hello) c.newInstance();
        h.say();
    }

    @Test
    public void testNothing() throws ClassNotFoundException, NoSuchMethodException {

        String content = "java.util.Set[Long]";
        String cc = new Objectex.RichString(content).ofClassTyee();
        System.out.println(cc);
    }

    @Test
    public void testParamIsDto() {
        how2UsingExample.testParamIsDto();
    }

    @Test
    public void testParamIsNotDtoAndMoreParam() {
        how2UsingExample.testParamIsNotDtoAndMoreParam();
    }

    @Test
    public void testParamIsNotDtoButOneParam() {
        how2UsingExample.testParamIsNotDtoButOneParam();
    }

    @Test
    public void testParamIsNotDtoButIsOneArray() {
        how2UsingExample.testParamIsNotDtoButIsOneArray();
    }

    @Test
    public void testParamIsNotDtoOthers() {
        how2UsingExample.testParamIsNotDtoOthers();
    }

    @Test
    public void testTempAdd() {
        how2UsingExample.testTempAdd();
    }
}