/*
 * Licensed to Author under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Author licenses this file to you under the Apache License,
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
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.velocity.VelocityManager;
import net.amg.jira.plugins.jrmp.services.model.ProjectOrFilter;
import net.amg.jira.plugins.jrmp.services.model.RiskIssues;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yahro01 on 7/6/15.
 */
@Service
public class RenderTemplateServiceImpl implements  RenderTemplateService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String MATRIX_SIZE_STRING = "matrixSize";
    private static final String PROJECT_NAME_STRING = "projectName";
    private static final String PROJECT_URL_STRING = "projectURL";
    private static final String PROJECT_LABEL_STRING = "projectLabel";
    private static final String RISK_DATE_LABEL_STRING = "riskDateLabel";
    private static final String UPDATED_LABEL = "updatedLabel";
    private static final String RED_TASKS_LABEL_STRING = "redTasksLabel";
    private static final String GREEN_TASKS_LABEL_STRING = "greenTasksLabel";
    private static final String YELLOW_TASKS_LABEL_STRING = "yellowTasksLabel";
    private static final String RED_TASKS_VALUE_STRING = "redTasksValue";
    private static final String GREEN_TASKS_VALUE_STRING = "greenTasksValue";
    private static final String YELLOW_TASKS_VALUE_STRING = "yellowTasksValue";
    private static final String DATE_STRING = "date";
    private static final String UPDATE_DATE_STRING = "updateDate";
    private static final String UPDATED_TASK_STRING = "updatedTask";
    private static final String UPDATED_TASK_URL_STRING = "updatedTaskUrl";
    private static final String OVERLOAD_COMMENT_MULTI_STRING = "overloadCommentMulti";
    private static final String OVERLOAD_COMMENT_MULTI_2_STRING = "overloadCommentMulti_2";
    private static final String OVERLOAD_COMMENT_SINGLE_STRING = "overloadCommentSingle";
    private static final String MATRIX_STRING = "matrix";
    private static final String CONSEQUENCE_LABEL_STRING = "consequenceLabel";
    private static final String PROBABILITY_LABEL_STRING = "probabilityLabel";
    private static final String MATRIX_TITLE = "matrixTitle";
    private static final String MATRIX_TEMPLATE = "matrixTemplate";
    private static final String TITLE_LABEL_STRING = "titleLabel";
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat UPDATE_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private I18nResolver i18nResolver;
    @Autowired
    private WebResourceUrlProvider webResourceUrlProvider;
    @Autowired
    private VelocityManagerFactory velocityFactory;
    @Autowired
    private VelocityContextFactory contextFactory;

    //4Spring dependency injection:
    public RenderTemplateServiceImpl() {}

    public String renderTemplate(ProjectOrFilter projectOrFilter, String matrixTitle, String matrixTemplate, RiskIssues riskIssues) {

        VelocityContext params = contextFactory.getDefaultContext();

        String title = projectOrFilter.getName();
        String url = "";

        if (projectOrFilter.isFilter()) {
            url = webResourceUrlProvider.getBaseUrl() + "/browse/?filter=" + projectOrFilter.getId();
        }

        if (projectOrFilter.isProject()) {
            url = webResourceUrlProvider.getBaseUrl() + "/browse/?project=" + projectOrFilter.getId();
        }

        params.put(PROBABILITY_LABEL_STRING, i18nResolver.getText("risk.management.matrix.probability_label"));
        params.put(CONSEQUENCE_LABEL_STRING, i18nResolver.getText("risk.management.matrix.consequence_label"));
        params.put(MATRIX_SIZE_STRING, RiskIssues.MATRIX_SIZE);
        params.put(PROJECT_NAME_STRING, title);
        params.put(PROJECT_URL_STRING, url);
        params.put(UPDATED_LABEL, i18nResolver.getText("risk.management.matrix.updated_label"));
        params.put(RISK_DATE_LABEL_STRING, i18nResolver.getText("risk.management.matrix.risk_date_label"));
        params.put(PROJECT_LABEL_STRING, i18nResolver.getText("risk.management.matrix.project_label"));
        params.put(RED_TASKS_VALUE_STRING, riskIssues.getRedTasks());
        params.put(GREEN_TASKS_VALUE_STRING, riskIssues.getGreenTasks());
        params.put(YELLOW_TASKS_VALUE_STRING, riskIssues.getYellowTasks());
        params.put(RED_TASKS_LABEL_STRING, i18nResolver.getText("risk.management.matrix.red_tasks_label"));
        params.put(GREEN_TASKS_LABEL_STRING, i18nResolver.getText("risk.management.matrix.green_tasks_label"));
        params.put(YELLOW_TASKS_LABEL_STRING, i18nResolver.getText("risk.management.matrix.yellow_tasks_label"));
        params.put(DATE_STRING, DATE_FORMATTER.format(new Date()));
        params.put(OVERLOAD_COMMENT_MULTI_STRING, i18nResolver.getText("risk.management.matrix.overload_comment_multi"));
        params.put(OVERLOAD_COMMENT_MULTI_2_STRING, i18nResolver.getText("risk.management.matrix.overload_comment_multi_2"));
        params.put(OVERLOAD_COMMENT_SINGLE_STRING, i18nResolver.getText("risk.management.matrix.overload_comment_single"));
        params.put(MATRIX_STRING, riskIssues.getListOfRows());
        params.put(MATRIX_TITLE, matrixTitle);
        params.put(MATRIX_TEMPLATE, matrixTemplate);
        params.put(TITLE_LABEL_STRING, i18nResolver.getText("risk.management.matrix.title_label"));
        Issue lastUpdatedIssue = riskIssues.getLastUpdatedIssue();
        if (lastUpdatedIssue != null) {
            params.put(UPDATE_DATE_STRING, UPDATE_DATE_FORMATTER.format(new Date(lastUpdatedIssue.getUpdated().getTime())));
            params.put(UPDATED_TASK_STRING, lastUpdatedIssue.getKey());
            params.put(UPDATED_TASK_URL_STRING, webResourceUrlProvider.getBaseUrl() + "/browse/" + lastUpdatedIssue.getKey());
        }
        VelocityManager velocityManager = velocityFactory.getVelocityManager();

        logger.info("generateMatrix: Everything went okay. Returning velocity Template.");
        return velocityManager.getEncodedBody("templates/", "matrixTemplate.vm", null, "UTF-8", params);
    }
}
