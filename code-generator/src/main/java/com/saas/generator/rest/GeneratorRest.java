package com.saas.generator.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.saas.generator.entity.R;
import com.saas.generator.service.GeneratorService;
import com.saas.generator.utils.GeneratorUtils;
import com.wubao.oshop.saas.common.msg.TableResultResponse;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by adam on 2017/8/25.
 */
@Controller
@RequestMapping("/base/generator")
public class GeneratorRest {

    @Autowired
    private GeneratorService generatorService;

    /**
     * 列表
     */
    @ResponseBody
    @RequestMapping("/page")
    public TableResultResponse<Map<String, Object>> list(@RequestParam Map<String, Object> params) {
        List<Map<String, Object>> result = generatorService.queryList(params);
        int total = generatorService.queryTotal(params);
        return new TableResultResponse(total, result);
    }

    /**
     * 生成代码
     */
    @RequestMapping("/code")
    public void code(HttpServletRequest request, HttpServletResponse response) throws IOException, ConfigurationException {
        String[] tableNames = new String[]{};
        String tables = request.getParameter("tables");
        tableNames = JSON.parseArray(tables).toArray(tableNames);

        byte[] data = generatorService.generatorCode(tableNames);

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"ag-admin-code.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IOUtils.write(data, response.getOutputStream());
    }


    /**
     * 生成代码
     */
    @PostMapping("/code2")
    public void code2(HttpServletRequest request, HttpServletResponse response) throws IOException, ConfigurationException {


        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        List<String> strings = IOUtils.readLines(reader);
        StringBuilder sb = new StringBuilder();
        strings.forEach(str->{
             sb.append(str);
        });
        JSONObject jsonObject = JSON.parseObject(sb.toString());

        String sql = jsonObject.getString("sql");
        String isOpt = jsonObject.getString("isOpt");
        String isSearch = jsonObject.getString("isSearch");
        String isPage = jsonObject.getString("isPage");

        System.out.println("sql="+sql);
        System.out.println("isOpt="+isOpt);
        System.out.println("isSearch="+isSearch);
        System.out.println("isPage="+isPage);

        String replace = sql.replace("\"", "");
        replace = replace.replaceAll("\r|\n|\t","");
        System.out.println(sql);
        System.out.println(replace);

        byte[] data = generatorService.generatorCode2(replace,isOpt,isSearch,isPage);


        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"ag-admin-code.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IOUtils.write(data, response.getOutputStream());
    }



    @ResponseBody
    @RequestMapping("/getTableInfo")
    public R getTableInfo(@RequestParam Map<String, Object> params) {
        return R.isOk().data(getData(params));
    }

    private LinkedHashMap<String, String> getData(Map<String, Object> params) {

        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        String[] tableNames = params.get("tableName").toString().split(",");
        System.out.println(Arrays.toString(tableNames));
        List<Map<String, String>> m_device = new ArrayList<>();

        for (String tableName : tableNames) {
            List<Map<String, String>> list = generatorService.queryColumns(tableName);
            m_device.addAll(list);
        }


        for (Map<String, String> stringStringMap : m_device) {

            String name = stringStringMap.get("columnName");
            name = GeneratorUtils.toLowerCaseFirstOne(GeneratorUtils.columnToJava(name));
            String value = stringStringMap.get("columnComment");
            if ("".equals(value)) {
                value = name;
            }
            result.put(name, value);
        }
        return result;
    }


    @ResponseBody
    @RequestMapping("/pageInfo")
    public R pageInfo(@RequestParam Map<String, Object> params) {
        LinkedHashMap<String, String> data = getData(params);
        return R.isOk().pageData(data);
    }

//    @ResponseBody
//    @RequestMapping(value = "/table")
//    public R table(@RequestParam Map<String, Object> map) {
//        Object sql = map.get("sql");
//        if (sql == null) {
//            return R.isOk().data("sql is null");
//        }
//
//        String mysql = sql.toString();
//
//        List<String> tableNames = GeneratorUtils.getTableNames(mysql);
//        System.out.println(mysql);
//        List<String> showCloum = null;
////        List<String> showCloum = GeneratorUtils.getShowCloum(mysql);
//
//        //总字段
//        List<Map<String, String>> totalCloun = new ArrayList<>();
//
//
//        for (String tableName : tableNames) {
//            List<Map<String, String>> list = generatorService.queryColumns(tableName);
//            totalCloun.addAll(list);
//        }
//
//        HashMap<String, String> columMap = new HashMap<>();
//
//        for (Map<String, String> stringStringMap : totalCloun) {
//            columMap.put(stringStringMap.get("columnName"), stringStringMap.get("columnComment"));
//        }
//
//        if ("*".equals(showCloum.get(0))) {
//            //显示所有的
//            return R.isOk().data(columMap);
//        }
//
//
//        LinkedHashMap<String, String> result = new LinkedHashMap<>();
//        for (String s : showCloum) {
//            result.put(s, columMap.get(s));
//        }
//
//        return R.isOk().data(result);
//
//
//    }


}
