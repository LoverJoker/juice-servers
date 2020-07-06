package com.saas.generator.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 表数据
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2017年08月25日
 */
@Getter
@Setter
public class TableEntity {
	//表的名称
	private String tableName;
	//表的名称
	private String className;
	//表的备注
	private String comments;
	//表的主键
	private ColumnEntity pk;
	//表的列名(不包含主键)
	private List<ColumnEntity> columns;
	
	//类名(第一个字母大写)，如：sys_user => SysUser
	private String classBigName;
	//类名(第一个字母小写)，如：sys_user => sysUser
	private String classSamllName;

}
