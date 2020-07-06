package com.saas.generator.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 列的属性
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2017年08月25日
 */
@Getter
@Setter
public class ColumnEntity {
	//列名
    private String columnName;
    //列名类型
    private String dataType;
    //列名备注
    private String comments;
    
    //属性名称(第一个字母大写)，如：user_name => UserName
    private String attrBigName;
    //属性名称(第一个字母小写)，如：user_name => userName
    private String attrSmallName;
    //属性类型
    private String attrType;
    //auto_increment
    private String extra;
    //别名
    private String columnAlias;


}
