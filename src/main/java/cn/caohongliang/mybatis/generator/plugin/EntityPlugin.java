package cn.caohongliang.mybatis.generator.plugin;

import cn.caohongliang.mybatis.generator.util.PluginUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * entity 插件
 *
 * @author caohongliang
 */
public class EntityPlugin extends PluginAdapter {
    private static final String PRIMARY_KEY_PACKAGE = "key";
    private static final String EXAMPLE_PACKAGE = "example";

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        List<String> javaDocLines = topLevelClass.getJavaDocLines();
        String defaultRemarks = topLevelClass.getType().getShortName();
        PluginUtils.classComment(javaDocLines, introspectedTable, defaultRemarks, " Entity");

        //该代码表示在生成class的时候，向topLevelClass添加一个@Setter和@Getter注解
        addLombokAnnotation(topLevelClass);
        topLevelClass.addAnnotation("@Builder");
        topLevelClass.addImportedType("lombok.Builder");
        return true;
    }

    private void addLombokAnnotation(TopLevelClass topLevelClass) {
        topLevelClass.addAnnotation("@Data");
        topLevelClass.addAnnotation("@NoArgsConstructor");
        topLevelClass.addAnnotation("@AllArgsConstructor");
        topLevelClass.addImportedType("lombok.Data");
        topLevelClass.addImportedType("lombok.NoArgsConstructor");
        topLevelClass.addImportedType("lombok.AllArgsConstructor");
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        List<String> javaDocLines = field.getJavaDocLines();
        javaDocLines.clear();
        //设置字段注释
        String remarks = PluginUtils.isEmpty(introspectedColumn.getRemarks()) ? field.getName() : introspectedColumn.getRemarks();
        javaDocLines.add("/**");
        javaDocLines.add(" * " + remarks);
        javaDocLines.add(" */");
        //设置字段注释
        field.addAnnotation("@ApiModelProperty(\"" + remarks + "\")");
        topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        //该方法在生成每一个属性的getter方法时候调用，如果我们不想生成getter，直接返回false即可；
        return false;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        //该方法在生成每一个属性的setter方法时候调用，如果我们不想生成setter，直接返回false即可；
        return false;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        //该代码表示在生成class的时候，向topLevelClass添加一个@Setter和@Getter注解
        addLombokAnnotation(topLevelClass);
        FullyQualifiedJavaType type = topLevelClass.getType();
        Class<? extends FullyQualifiedJavaType> typeClass = type.getClass();
        try {
            java.lang.reflect.Method method = typeClass.getDeclaredMethod("simpleParse", String.class);
            method.setAccessible(true);
            String primaryKeyType = type.getPackageName() + wrapper(PRIMARY_KEY_PACKAGE) + type.getShortNameWithoutTypeArguments();
            method.invoke(type, primaryKeyType);
            introspectedTable.setPrimaryKeyType(primaryKeyType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //移动example文件到entity.example中
        FullyQualifiedJavaType type = topLevelClass.getType();
        String exampleType = type.getPackageName() + wrapper(EXAMPLE_PACKAGE) + type.getShortNameWithoutTypeArguments();
        PluginUtils.setMethodValue(type, "simpleParse", exampleType, String.class);
        introspectedTable.setExampleType(exampleType);
        return true;
    }

    private String wrapper(String str) {
        return "." + str + ".";
    }
}
