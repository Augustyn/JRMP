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
package net.amg.jira.plugins.jrmp.rest.model;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.sal.api.message.I18nResolver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.amg.jira.plugins.jrmp.services.model.DateModel;
import net.amg.jira.plugins.jrmp.services.model.ProjectOrFilter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jonatan on 30.05.15.
 */
@Slf4j
public class MatrixRequest {

    public static final String BUNDLE_ERROR_EMPTY_FILTER = "risk.management.validation.error.empty_filter";
    public static final String BUNDLE_ERROR_FILTER_IS_INCORRECT = "risk.management.validation.error.filter_is_incorrect";
    public static final String BUNDLE_ERROR_EMPTY_DATE = "risk.management.validation.error.empty_date";

    @Setter @Getter
    private String filter;
    @Setter @Getter
    private String title;
    @Setter @Getter
    private String date;
    @Setter @Getter
    private String template;
    @Setter @Getter
    private String refreshRate;
    @Setter @Getter
    private ProjectOrFilter projectOrFilter;
    @Setter @Getter
    private DateModel dateModel;

    public ErrorCollection doValidation(I18nResolver i18nResolver, JiraAuthenticationContext authenticationContext, SearchService searchService, OfBizDelegator ofBizDelegator) {
        ErrorCollection errorCollection = new ErrorCollection();

        if (StringUtils.isBlank(filter)) {
            errorCollection.addError(GadgetFieldEnum.FILTER.toString(), i18nResolver.getText(BUNDLE_ERROR_EMPTY_FILTER));
            log.info("Filter cannot be blank.");
        } else {
            projectOrFilter = ProjectOrFilter.createProjectOrFilter(filter,ofBizDelegator);
            if (!projectOrFilter.isValid()) {
                errorCollection.addError(GadgetFieldEnum.FILTER.toString(), i18nResolver.getText(BUNDLE_ERROR_FILTER_IS_INCORRECT));
                log.info("Project of filter field is invalid for given field:" + projectOrFilter);
            } else {
                if (projectOrFilter.getQuery() == null) {
                    errorCollection.addError(GadgetFieldEnum.FILTER.toString(), i18nResolver.getText(BUNDLE_ERROR_FILTER_IS_INCORRECT));
                    log.info("Cannot get filter for given ProjectOrFilter filed query: " + projectOrFilter.getQuery());
                } else {
                    MessageSet messageSet = searchService.validateQuery(authenticationContext.getLoggedInUser(), projectOrFilter.getQuery());
                    if (messageSet.hasAnyErrors()) {
                        log.warn("Query is invalid. Enable info for search errors list");
                        if (log.isInfoEnabled()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Search error messages: \n");
                            for (String msg : messageSet.getErrorMessagesInEnglish()) {
                                sb.append(msg +"\n");
                            }
                            log.info(sb.toString());
                        }
                        errorCollection.addError(GadgetFieldEnum.FILTER.toString(), i18nResolver.getText(BUNDLE_ERROR_FILTER_IS_INCORRECT));
                    }
                }
            }
        }

        if (StringUtils.isBlank(date)) {
            errorCollection.addError(GadgetFieldEnum.DATE.toString(), i18nResolver.getText(BUNDLE_ERROR_EMPTY_DATE));
            log.info("Date field is blank");
        }

        try {
            dateModel = DateModel.valueOf(date);
        } catch (NullPointerException e) {
            errorCollection.addError(GadgetFieldEnum.DATE.toString(), i18nResolver.getText("risk.management.validation.error.wrong_date"));
            log.info("Invalid date field: " + date);
        }

        if (StringUtils.isBlank(refreshRate)) {
            errorCollection.addError(GadgetFieldEnum.REFRESH.toString(), i18nResolver.getText("risk.management.validation.error.empty_refresh"));
            log.info("refresh rate field is blank");
        }

        return errorCollection;
    }

    public Map<String,String> getParameters()
    {
        Map<String,String> parameters = new HashMap<String, String>();
        parameters.put(GadgetFieldEnum.DATE.toString(),this.date);
        parameters.put(GadgetFieldEnum.FILTER.toString(),this.filter);
        parameters.put(GadgetFieldEnum.REFRESH.toString(),this.refreshRate);
        parameters.put(GadgetFieldEnum.TEMPLATE.toString(),this.template);
        parameters.put(GadgetFieldEnum.TITLE.toString(),this.title);
        return parameters;
    }
}
