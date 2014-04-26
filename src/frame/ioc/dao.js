/**
 * 
 * 数据库连接的配置文件，包括：
 * 
 * dataSource： 用于连接数据库 sqlFile： 用于读取自定义sql文件，相当于iBatis的sql配置文件 dao：
 * Nutz框架的数据库操作封装类，不能使用sqlFile里的语句 daoWithSqlMap：
 * Nutz框架的数据库操作封装类，可以使用sqlFile里的语句
 * 
 * 所有这些都是零件，可以随意组合使用，例如： dao这个大零件，是用dataSource这个小零件组合而成的
 * daoWithSqlMap是用dataSource和sqlFile组合而成的
 */
var ioc = {

	dataSource : {
		type : "com.alibaba.druid.pool.DruidDataSource",
		events : {
			create : "init",
			depose : "close"
		},
		fields : {
			url : "jdbc:mysql://127.0.0.1:3306/zxq",
			username : "root",
			password : "root"
		}
	},

	dao : {
		type : "org.nutz.dao.impl.NutDao",
		args : [ {
			refer : "dataSource"
		} ]
	},
	tran : {
		type : "org.nutz.aop.interceptor.TransactionInterceptor"
	},
	annotationAop : {
		type : "org.nutz.ioc.aop.config.impl.AnnotationAopConfigration"
	},
	$aop : {
		type : "org.nutz.ioc.aop.config.impl.ComboAopConfigration",
		fields : {
			aopConfigrations : [ {
				refer : "annotationAop"
			} ]
		}
	},
	stormClientManager : {
		type : "frame.manager.StormClientManager",
		events : {
			create : "init",
			depose : "shutdown"
		}
	},
	fileUtil : {
		type : "frame.util.FileUtil",
		fields : {
			context : {
				app : '$servlet'
			}
		}
	}
};