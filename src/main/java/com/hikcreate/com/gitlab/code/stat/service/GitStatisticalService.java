package com.hikcreate.com.gitlab.code.stat.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.hikcreate.com.gitlab.code.stat.common.Response;
import com.hikcreate.com.gitlab.code.stat.common.ResponseGenerator;
import com.hikcreate.com.gitlab.code.stat.config.GitLabConfigBean;
import com.hikcreate.com.gitlab.code.stat.service.param.response.GitStatisticalRes;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.CommitStats;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author zhaodongxx
 * @date 2020/12/21 17:51
 */
@Service
@Slf4j
public class GitStatisticalService {

    @Autowired
    private GitLabConfigBean gitLabConfigBean;

    /**
     * @param since         统计开始时间
     * @param until         统计开始时间
     * @param branch        统计的分支
     * @param authors       统计的作者列表
     * @param authorEmails  统计的作者邮箱列表
     * @param projectNames  统计的项目名列表
     * @param owner         统计的项目拥有者
     * @param namespaceName 统计的项目命名空间
     * @return 代码的提交数据
     */
    public Response<GitStatisticalRes> statistical(String since,
                                                   String until,
                                                   String branch,
                                                   List<String> authors,
                                                   List<String> authorEmails,
                                                   List<String> projectNames,
                                                   String owner,
                                                   String namespaceName) {

        Map<String, Object> res = new HashMap<>();
        Map<String, Integer> totalMap = new HashMap<>();

        // 建立 GitLab 连接、获取所有项目
        GitLabApi gitLabApi = new GitLabApi(gitLabConfigBean.getHost(), gitLabConfigBean.getToken());
        gitLabApi.enableRequestResponseLogging(Level.FINE);
        // Get the list of projects your account has access to
        List<Project> allProjects = null;
        try {
            allProjects = gitLabApi.getProjectApi().getProjects();
        } catch (GitLabApiException e) {
            e.printStackTrace();
            return ResponseGenerator.success4Msg("连接失败");
        }
        log.info("==>  建立 GitLab【{}】连接，并获取所有的项目成功，共计【{}】个项目", gitLabConfigBean.getHost(), allProjects.size());

        // 筛选项目
        List<Project> matchGitlabProjects = getMatchGitlabProjects(allProjects, owner, namespaceName, projectNames);

        if (CollectionUtils.isEmpty(matchGitlabProjects)) {
            return ResponseGenerator.success4Msg("未找到匹配的项目");
        }

        for (Project project : matchGitlabProjects) {
            Map<String, Integer> projectStat = new HashMap<>();
            Integer projectId = project.getId();
            String name = project.getName();
            log.info(name + " 统计开始");
            try {
                // 查询commit列表
                List<Commit> commits = gitLabApi.getCommitsApi().getCommits(projectId, branch, DateUtil.parseDate(since), DateUtil.parseDate(until), null, true, true, false);
                if (CollectionUtils.isEmpty(commits)) {
                    log.info(name + " 统计结束，没有满足条件的提交");
                    continue;
                }
                log.info(name + " 提交了 " + (CollectionUtils.isEmpty(commits) ? 0 : commits.size()) + " 次");

                // 筛选提交
                List<Commit> matchCommits = getMatchCommits(commits, authors, authorEmails);
                if (CollectionUtils.isEmpty(matchCommits)) {
                    log.info(name + " 统计结束，没有满足条件的提交");
                    continue;
                }

                for (Commit commit : matchCommits) {
                    Commit commitAndStat = gitLabApi.getCommitsApi().getCommit(projectId, commit.getId());
                    CommitStats stats = commitAndStat.getStats();
                    if (stats == null) {
                        continue;
                    }

                    Integer total = stats.getTotal();
                    String username = commitAndStat.getAuthorName() + "(" + commitAndStat.getAuthorEmail() + ")";

                    projectStat.compute(username, (k, v) -> (v == null ? 0 : v) + total);
                    totalMap.compute(username, (k, v) -> (v == null ? 0 : v) + total);
                }
                if (!CollectionUtils.isEmpty(projectStat)) {
                    res.put(name, projectStat);
                }
            } catch (GitLabApiException e) {
                e.printStackTrace();
                continue;
            }
            log.info(name + " 统计完成");
        }

        // 排序
        LinkedHashMap<String, Integer> collect = totalMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        res.put("total", collect);
        return ResponseGenerator.success(new GitStatisticalRes().setStat(res));
    }


    /**
     * 筛选匹配的项目
     */
    private List<Project> getMatchGitlabProjects(List<Project> allProjects,
                                                 String owner,
                                                 String namespaceName,
                                                 List<String> projectNames) {
        // 筛选匹配名称的项目
        List<Project> matchList = allProjects;
        if (!CollectionUtils.isEmpty(projectNames)) {
            log.info("==>  筛选指定 name=【{}】 的全部项目", projectNames.toString());
            matchList = allProjects.stream().filter(m -> projectNames.contains(m.getName())).collect(Collectors.toList());
        }

        // 筛选匹配 owner 的项目
        if (!StrUtil.isBlank(owner)) {
            log.info("==>  筛选指定 owner=【{}】 的全部项目", owner);
            matchList = matchList.stream().filter(m -> m.getOwner().getName().equals(owner)).collect(Collectors.toList());
        }

        // 筛选指定 namespaceName 的项目
        if (!StrUtil.isBlank(namespaceName)) {
            log.info("==>  筛选指定 namespaceName=【{}】 的全部项目", namespaceName);
            matchList = matchList.stream().filter(m -> m.getNamespace().getName().equals(namespaceName)).collect(Collectors.toList());
        }

        log.info("==>  满足筛选条件的共计【{}】个项目", matchList.size());

        return matchList;
    }

    /**
     * 筛选匹配的提交
     */
    private List<Commit> getMatchCommits(List<Commit> allCommits, List<String> authors, List<String> authorEmails) {

        List<Commit> matchList = allCommits;

        // 移除合并分支的提交
        matchList = matchList.stream().filter(c -> !c.getMessage().contains("Merge branch")).collect(Collectors.toList());

        // 筛选匹配author的项目
        if (!CollectionUtils.isEmpty(authors)) {
            log.info("==>  筛选指定 author=【{}】 的全部项目", authors.toString());
            matchList = matchList.stream().filter(c -> authors.contains(c.getAuthorName())).collect(Collectors.toList());
        }

        // 筛选匹配email的项目
        if (!CollectionUtils.isEmpty(authorEmails)) {
            log.info("==>  筛选指定 authorEmail=【{}】 的全部项目", authorEmails.toString());
            matchList = matchList.stream().filter(c -> authorEmails.contains(c.getAuthorEmail())).collect(Collectors.toList());
        }

        log.info("==>  满足筛选条件的共计【{}】个提交", matchList.size());

        return matchList;
    }
}
