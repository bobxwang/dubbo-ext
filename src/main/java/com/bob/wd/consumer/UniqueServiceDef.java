package com.bob.wd.consumer;

import com.bob.wd.consumer.Objectex.*;

/**
 * Created by wangxiang on 17/11/29.
 */
public class UniqueServiceDef {

    /**
     * service reference group
     */
    private String group;

    /**
     * service reference interface, should include the package name
     */
    private String interfaceName;

    /**
     * service reference version
     */
    private String version;

    /**
     * call method
     */
    private String method;

    /**
     * input dto,不为空时必须设置serviceDtoProperty属性
     */
    private String inputDto;

    /**
     * DTO对象属性,以逗号进行分隔
     */
    private String dtoProperty;

    /**
     * 参数名称,多个时以逗号分隔,不为空时必须设置serviceParamType属性
     */
    private String inputEnum;

    /**
     * 参数类型或者DTO的属性类型,多个时以逗号分隔,跟serviceInputEnum/dtoProperty一起使用
     */
    private String paramType;

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }


    public String getInputDto() {
        return inputDto;
    }

    public void setInputDto(String inputDto) {
        this.inputDto = inputDto;
    }

    public String getDtoProperty() {
        return dtoProperty;
    }

    public void setDtoProperty(String dtoProperty) {
        this.dtoProperty = dtoProperty;
    }

    public String getInputEnum() {
        return inputEnum;
    }

    public void setInputEnum(String inputEnum) {
        this.inputEnum = inputEnum;
    }

    public UniqueServiceDef(String interfaceName,
                            String method, String version, String group,
                            String inputDto, String dtoProperty,
                            String inputEnum, String paramType) {
        this();
        this.group = group;
        this.interfaceName = interfaceName;
        this.version = version;
        this.method = method;
        this.inputDto = inputDto;
        this.inputEnum = inputEnum;
        this.dtoProperty = dtoProperty;
        this.paramType = paramType;
    }

    public UniqueServiceDef() {
        super();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UniqueServiceDef{");
        sb.append("group='").append(group).append('\'');
        sb.append(", interfaceName='").append(interfaceName).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", method='").append(method).append('\'');
        sb.append(", inputDto='").append(inputDto).append('\'');
        sb.append(", dtoProperty='").append(dtoProperty).append('\'');
        sb.append(", inputEnum='").append(inputEnum).append('\'');
        sb.append(", paramType='").append(paramType).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * 返回实例的JSON传输类型
     *
     * @return
     */
    public Object toExample() {
        return new RichUniqueServiceDef(this).check();
    }
}