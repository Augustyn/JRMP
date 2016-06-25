/*
 * Licensed to AMG.net under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * AMG.net licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.amg.jira.plugins.jrmp.services;

import com.atlassian.jira.issue.Issue;
import lombok.extern.slf4j.Slf4j;
import net.amg.jira.plugins.jrmp.services.model.DateModel;
import net.amg.jira.plugins.jrmp.services.model.ProjectOrFilter;
import net.amg.jira.plugins.jrmp.services.model.RiskIssues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MatrixGeneratorImpl implements MatrixGenerator {
    @Autowired
    private JRMPSearchService jrmpSearchService;
    @Autowired
    private RenderTemplateService renderTemplate;
    @Autowired
    private RiskIssuesFinder riskIssuesFinder;

    public MatrixGeneratorImpl() {}//Empty constructor 4spring.

    @Override
    public String generateMatrix(ProjectOrFilter projectOrFilter, String matrixTitle, String matrixTemplate, DateModel dateModel) {
        long start = System.currentTimeMillis();
        log.info("generateMatrix: Method start");
        List<Issue> issues = jrmpSearchService.getAllQualifiedIssues(projectOrFilter.getQuery(), dateModel);
        log.info("Found {} issues, in {}ms", issues.size(), (System.currentTimeMillis() - start));
        RiskIssues riskIssues = riskIssuesFinder.fillAllFields(issues, projectOrFilter.getQuery(), dateModel);
        final String template = renderTemplate.renderTemplate(projectOrFilter, matrixTitle, matrixTemplate, riskIssues);
        log.debug("Returning template string in: {}ms", (System.currentTimeMillis() - start));
        return template;
    }

}
