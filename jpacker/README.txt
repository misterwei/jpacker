jpacker 是一个 简单的jdbc orm 框架
jpacker 的目标：是用简单的sql语句实现跨数据库的操作，支持延迟加载，支持注解配置。

版本0.0.2
在ThreadLocal里，第一个调用begin方法返回true的 JdbcExecutor 在关闭或者提交回滚前，其他的JdbcExecutor都没有提交回滚事务的权限，即使调用了也等于空方法

版本0.0.1
支持数据库 sqlserver2000,sqlserver2005,mysql,postgresql,详细支持查看 jpacker.local 目录
使用层c3p0连接池作为数据库连接的管理工具


主要类说明：
JdbcExecutor 该类不可以跨线程使用，所有的数据库操作都是基于这个类来进行
JdbcExecutorFactory 该类线程安全，可以跨线程使用，主要负责生成 JdbcExecutor
JdbcExecutorUtils   该类是静态类，在只有一个数据库的情况下可以使用这个类


简单使用方法：

1.配置连接池，并实例化JdbcExecutorFactory
ComboPooledDataSource cpds =  new ComboPooledDataSource();
cpds.setDriverClass("net.sourceforge.jtds.jdbc.Driver");
cpds.setUser("xxxx");
cpds.setPassword("xxxxxx");
cpds.setJdbcUrl("jdbc:jtds:sqlserver://127.0.0.1:1433/jpacker_test");
cpds.setMinPoolSize(20);
cpds.setMaxPoolSize(100);
cpds.setMaxStatements(500);
cpds.setCheckoutTimeout(1800);

List list = new ArrayList();
list.add(TestModel.class);

//使用JdbcExecutorUtils 实例化 JdbcExecutorFactory
JdbcExecutorUtils.instanceConfiguration(cpds, list,new MSSQL2005Executor());


2.获取JdbcExecutor 并执行数据库操作，执行完后，必须调用close
JdbcExecutor jdbc = JdbcExecutorUtils.getJdbcExecutor();
TestModel recs = jdbc.queryOne(TestModel.class, "select * from users",null);
jdbc.close();


将每个业务类使用的JdbcExecutor 都用一个数据库连接
1.首先 调用 JdbcExecutorUtils.beginThreadLocal(); 启用ThreadLocal
2. 这里面调用的JdbcExecutor 都是使用的一个 数据库连接
3. 调用 JdbcExecutorUtils.endThreadLocal(); 结束ThreadLocal


