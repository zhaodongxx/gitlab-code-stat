package com.hikcreate.com.gitlab.code.stat.controller;

import com.hikcreate.com.gitlab.code.stat.common.Response;
import com.hikcreate.com.gitlab.code.stat.service.GitStatisticalService;
import com.hikcreate.com.gitlab.code.stat.service.param.response.GitStatisticalRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhaodongxx
 * @date 2020/12/21 17:44
 */
@RestController
@RequestMapping("/gitlab")
public class GitStatisticalController {

    @Autowired
    private GitStatisticalService gitStatisticalService;

    @GetMapping("/codeStatistics")
    public Response<GitStatisticalRes> statistical(@RequestParam(value = "since") String since,
                                                   @RequestParam(value = "until") String until,
                                                   @RequestParam(value = "branch", required = false, defaultValue = "master") String branch,
                                                   @RequestParam(value = "authors", required = false) List<String> authors,
                                                   @RequestParam(value = "authorEmails", required = false) List<String> authorEmails,
                                                   @RequestParam(value = "projectNames", required = false) List<String> projectNames,
                                                   @RequestParam(value = "owner", required = false, defaultValue = "") String owner,
                                                   @RequestParam(value = "namespaceName", required = false, defaultValue = "") String namespaceName) {
        return gitStatisticalService.statistical(since, until, branch, authors, authorEmails, projectNames, owner, namespaceName);
    }
}
