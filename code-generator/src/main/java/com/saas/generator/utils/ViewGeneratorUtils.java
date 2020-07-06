package com.saas.generator.utils;

import com.saas.generator.entity.ColumnEntity;
import com.saas.generator.entity.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月19日 下午11:40:24
 */
public class ViewGeneratorUtils {


    //根据sql，获取表名
    public static List<String> getTableNames(String sql){

        Iterator<String> iterator = getStringIterator(sql);
        List<String>tables = new ArrayList<>();

        while (iterator.hasNext()){

            String it = iterator.next();
            if(it.equalsIgnoreCase("FROM")){
                String next = iterator.next();
                if(!"".equals(next)){
                    tables.add(next);
                }
            }else if(it.equalsIgnoreCase("JOIN")){
                String next = iterator.next();
                if(!"".equals(next)){
                    tables.add(next);
                }
            }
        }
        return tables;
    }

    public static List<HashMap<String,String>> getShowCloum(String sql){
        int select = sql.indexOf("select");
        if(select==-1){
            select=sql.indexOf("SELECT");
        }

        int from = sql.indexOf("from");
        if(from==-1){
            from=sql.indexOf("FROM");
        }

        String substring = sql.substring(select+6, from);
        String[] split = substring.split(",");

        List<HashMap<String,String>> map = new ArrayList<>();

        for (String s : split) {
            String[] s1 = s.trim().split(" ");
            HashMap<String, String> e = new HashMap<>();
            e.put("columnName",s1[0]);
            if(s1.length>1){
                e.put("columnAlias",s1[1]);
            }else {
                e.put("columnAlias",s1[0]);
            }
            map.add(e);
        }

        return map;
    }

    private static Iterator<String> getStringIterator(String sql) {
        String[] s = sql.split(" ");
        List<String> x = new ArrayList<>();
        for (String s1 : s) {
            if (s1 != null && !s1.trim().equals("")) {
                x.add(s1);
            }
        }
        return x.iterator();
    }




    private ViewGeneratorUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<String> getTemplates() {
        List<String> templates = new ArrayList();
        templates.add("template/index.js.vm");
        templates.add("template/index.vue.vm");
//        templates.add("template/mapper.xml.vm");
//        templates.add("template/biz.java.vm");
//        templates.add("template/entity.java.vm");
//        templates.add("template/mapper.java.vm");
//        templates.add("template/controller.java.vm");
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table,
                                     List<Map<String, String>> columns,
                                     ZipOutputStream zip,
                                     String isOpt,
                                     String isSearch,
                                     String isPage
    ) throws IOException, ConfigurationException {
        //配置信息
        Configuration config = getConfig();

        //表信息
        TableEntity tableEntity = new TableEntity();
        String tableName = table.get("tableName");
        if(tableName.startsWith("o_")){
            tableName = tableName.substring(2);
        }
        tableEntity.setTableName(tableName);
        tableEntity.setComments(table.get("tableComment"));
        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
        tableEntity.setClassBigName(className);
        tableEntity.setClassSamllName(StringUtils.uncapitalize(className));

        //列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            System.out.println(column.toString());
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));

            columnEntity.setExtra(column.get("extra"));
            String columnAlias = column.get("columnAlias");

            if(columnAlias!=null && !columnAlias.contains("_")){
                columnEntity.setColumnAlias(columnAlias);
            }else {
                columnEntity.setColumnAlias(StringUtils.uncapitalize(columnToJava(columnAlias)));
            }

            String columnComment = column.get("columnComment");
            if("".equals(columnComment)){
                columnComment = columnEntity.getColumnAlias();
            }
            columnEntity.setComments(columnComment);

            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setAttrBigName(attrName);
            columnEntity.setAttrSmallName(StringUtils.uncapitalize(attrName));


            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), "unknowType");
            columnEntity.setAttrType(attrType);

            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }

            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        //没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassBigName());
        map.put("classname", tableEntity.getClassSamllName());
        map.put("pathName", tableEntity.getClassSamllName().toLowerCase());
        map.put("columns", tableEntity.getColumns());

        map.put("package", config.getString("package"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        map.put("moduleName", config.getString("mainModule"));
        map.put("secondModuleName", toLowerCaseFirstOne(className));

        map.put("isOpt", getOptValue(isOpt));
        map.put("isSearch", getOptValue(isSearch));
        map.put("isPage", getOptValue(isPage));

        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            //添加到zip
            zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassBigName(), config.getString("package"), config.getString("mainModule"))));
            IOUtils.write(sw.toString(), zip, "UTF-8");
            IOUtils.closeQuietly(sw);
            zip.closeEntry();
        }
    }


    private static Boolean getOptValue(String isOpt) {
        return isOpt == null || "1".equals(isOpt);
    }

    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replace(tablePrefix, "");
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() throws ConfigurationException {
        return new PropertiesConfiguration("generator.properties");
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName, String moduleName) {

        String frontPath = "ui" + File.separator;

        if (template.contains("index.js.vm")) {
            return frontPath + "api" + File.separator + moduleName + File.separator + toLowerCaseFirstOne(className) + File.separator + "index.js";
        }

        if (template.contains("index.vue.vm")) {
            return frontPath + "views" + File.separator + moduleName + File.separator + toLowerCaseFirstOne(className) + File.separator + "index.vue";
        }


        return null;
    }

    //首字母转小写
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    public static void main(String[] args) {
//        List<String> showCloum = getShowCloum("select * from o_product");
//        System.out.println(showCloum.get(0));
        String columnAlias =columnToJava("columnAlias");
        System.out.println(columnAlias);


    }

}
