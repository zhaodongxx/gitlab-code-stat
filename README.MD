# 简介
该项目用于统计`gitlab`平台上用户的代码提交行数。可以指定统计的时间范围、分支、作者名称、作者邮箱等。

统计的逻辑是通过`gitlab`提供的接口遍历目标仓库指定分支指定时间范围内的所有commit，单次commit的代码行数等于增加的行数+修改的行数+删除的行数。
# 如何使用

```yaml
# 修改为自己的相关配置即可。
# token一定要拥有要统计仓库的访问权限；
gitlab:
  host: http://git.xxx.com
  token: xxx
```

调用 `gitlab/codeStatistics` 接口即可，接口定义查看 `GitStatisticalController` 类。
