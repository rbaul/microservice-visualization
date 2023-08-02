package com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos;

import lombok.Data;

import java.util.List;

/**
 * {
 *         "size": 3,
 *         "limit": 3,
 *         "isLastPage": false,
 *         "values": [
 *             {  result 0  },
 *              {  result 1  },
 *              {  result 2  }
 *              ],
 *          "start":0,
 *          "filter":null,
 *          "nextPageStart":3
 * }
 */
@Data
public class PagedDto<T> {
    private Integer size;
    private Integer limit;
    private Boolean isLastPage;
    private List<T> values;
    private Integer start;
    private Integer nextPageStart;
    private String filter;
}
