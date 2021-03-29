package com.hikcreate.com.gitlab.code.stat.service.param.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author zhaodongxx
 * @date 2020/12/21 18:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GitStatisticalRes {

    private Map<String, Object> stat;
}
