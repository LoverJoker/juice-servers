package com.saas.generator.service;

import com.saas.generator.mapper.GeneratorMapper;
import com.saas.generator.utils.GeneratorUtils;
import com.saas.generator.utils.ViewGeneratorUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2017年08月25日
 */
@Service
public class GeneratorService {
    @Autowired
    private GeneratorMapper generatorMapper;

    public static String[] deleteBlank(String[] arr) {
        List<String> collect = Arrays.asList(arr).stream().filter(s -> !"".equals(s.trim())).collect(Collectors.toList());
        return collect.toArray(new String[]{});
    }

    public List<Map<String, Object>> queryList(Map<String, Object> map) {
        int offset = Integer.parseInt(map.get("offset").toString());
        int limit = Integer.parseInt(map.get("limit").toString());
        map.put("offset", offset);
        map.put("limit", limit);
        return generatorMapper.queryList(map);
    }

    public int queryTotal(Map<String, Object> map) {
        return generatorMapper.queryTotal(map);
    }


    public Map<String, String> queryTable(String tableName) {
        if (isMaster(tableName)) {
            return generatorMapper.masterQueryTable(tableName);
        }
        return generatorMapper.queryTable(tableName);
    }


    public List<Map<String, String>> queryColumns(String tableName) {
        if (isMaster(tableName)) {
            return generatorMapper.masterQueryColumns(tableName);
        }
        return generatorMapper.queryColumns(tableName);
    }


    public byte[] generatorCode(String[] tableNames) throws IOException, ConfigurationException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);

        for (String tableName : tableNames) {
            //查询表信息
            Map<String, String> table = queryTable(tableName);
            //查询列信息
            List<Map<String, String>> columns = queryColumns(tableName);

            //生成代码
            GeneratorUtils.generatorCode(table, columns, zip);
        }
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }


    public byte[] generatorCode2(String sql, String isOpt, String isSearch, String isPage) throws IOException, ConfigurationException {

        List<String> tableNames = ViewGeneratorUtils.getTableNames(sql);
        List<Column> allColumn = getAllColumn(sql);

        List<Map<String, String>> columns = new ArrayList<>();
        HashMap<String, List<Map<String, String>>> columnsMap = new HashMap<>();

        for (String tableName : tableNames) {
            List<Map<String, String>> list = queryColumns(tableName);
            columns.addAll(list);
            columnsMap.put(tableName, list);
        }

        List<Map<String, String>> filterList;

        if (allColumn.get(0).isAll) {
            columns.forEach(cls -> {
                cls.put("columnAlias", cls.get("columnName"));
            });
            filterList = columns;
        } else {

            //设置别名
            filterList = new ArrayList<>();

            allColumn.forEach(skey -> {
                List<Map<String, String>> cls;
                if (skey.table == null) {
                    cls = columns;
                } else {
                    cls = columnsMap.get(skey.table.name);
                }

                cls.forEach(cmap -> {
                    if (skey.name.equalsIgnoreCase(cmap.get("columnName"))) {
                        cmap.put("columnAlias", skey.alias);
                        filterList.add(cmap);
                    }
                });

            });

        }


        filterList.forEach((f -> {
            String columnComment = f.get("columnComment");
            if (!"".equals(columnComment)) {
                if (columnComment.contains("(")) {
                    columnComment = columnComment.substring(0, columnComment.indexOf("("));
                } else if (columnComment.contains("（")) {
                    columnComment = columnComment.substring(0, columnComment.indexOf("（"));
                }
            }
            f.put("columnComment", columnComment);
        }));


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);


        String tableName = tableNames.get(0);
        Map<String, String> table = queryTable(tableName);
        ;


        //生成代码
        ViewGeneratorUtils.generatorCode(table, filterList, zip, isOpt, isSearch, isPage);
        IOUtils.closeQuietly(zip);

        return outputStream.toByteArray();
    }


    public static List<Column> getAllColumn(String sql) {
        List<Column> allColumn = Column.getAllColumn(sql);
        List<String> allTableAlias = Column.getAllTableAlias(allColumn);
        HashMap<String, Table> map = new HashMap<>();
        Table.getTableList(sql, allTableAlias, map);
        Column.fillTable(allColumn, map);
        return allColumn;
    }

    public static boolean isMaster(String tableName) {
        return !tableName.startsWith("o_");
    }

    public static void main(String[] args) {
        String sql = "select * from o_product";
    }


    public static class Table {
        public String alias;//
        public String name;
        public boolean isMarster;//是否是主库

        public boolean isAlias() {
            return alias != null && !alias.equalsIgnoreCase(name);
        }

        public static List<Table> getTableList(String sql, List<String> aliasList, HashMap<String, Table> tableMap) {

            String[] split1 = sql.substring(sql.toLowerCase().indexOf("from")).trim().split(" ");
            List<String> collect = Arrays.asList(split1).stream().filter(k -> !"".equals(k.trim())).collect(Collectors.toList());
            Iterator<String> iterator = collect.iterator();

            List<Table> tableList = new ArrayList<>();

            String next = null;
            while (iterator.hasNext()) {

                if (next == null) {
                    next = iterator.next();
                }

                if ("from".equalsIgnoreCase(next) || "join".equalsIgnoreCase(next)) {
                    String next1 = iterator.next();

                    Table table1 = new Table();
                    table1.name = next1;
                    table1.isMarster = !next1.startsWith("o_");

                    tableList.add(table1);

                    if (!iterator.hasNext()) {
                        table1.alias = next1;
                        break;
                    }

                    String next2 = iterator.next();
                    if (aliasList.contains(next2)) {
                        table1.alias = next2;
                        tableMap.put(next2, table1);
                        System.out.println("next2=" + next2);
                    } else {
                        table1.alias = next2;
                        next = next2;
                        continue;
                    }
                }

                next = null;

            }

            return tableList;

        }

    }

    public static class Column {

        public Table table;
        public String tableAlias;
        public String name;
        public String alias;
        public String orgField;//原始字段
        public String comment;//字典描述
        public boolean isAll = false;

        public static void fillTable(List<Column> columnList, HashMap<String, Table> tableHashMap) {

            columnList.forEach(column -> {
                        if (!"".equals(column.tableAlias)) {
                            column.table = tableHashMap.get(column.tableAlias);
                        }
                    }
            );


        }

        public static List<Column> getAllColumn(String sql) {

            List<Column> columnList = new ArrayList<>();

//        1.获取所有的字段
            int select = sql.toLowerCase().indexOf("select");

            String from = sql.substring(select + 6, sql.toLowerCase().indexOf("from"));

            //获取表别名

            String[] split = from.split(",");


            //别名获取到了
            List<String> tableOtheName = new ArrayList<>();
            for (String s : split) {
                columnList.add(new Column(s.trim()));
            }

            return columnList;
        }


        //获取所有表的别名
        public static List<String> getAllTableAlias(List<Column> list) {

            List<String> newList = new ArrayList<>();
            list.forEach(c -> {
                if (c.tableAlias != null) {
                    newList.add(c.tableAlias);
                }
            });
            return newList;
        }

        public Column(String orgField) {
            this.orgField = orgField;
            if ("*".equalsIgnoreCase(orgField)) {
                isAll = true;
                return;
            }

            String[] s1 = orgField.trim().split(" ");
            s1 = deleteBlank(s1);

            String s2 = s1[0];

            if (s2.contains(".")) {
                String[] s3 = s2.split("\\.");
                //获取到了表别名
                this.tableAlias = s3[0];
                this.name = s3[1];
            } else {
                this.name = s2;
            }

            if (s1.length > 1) {
                //获取到了别名
                this.alias = s1[s1.length - 1];
            } else {
                this.alias = this.name;
            }

        }
    }


}
