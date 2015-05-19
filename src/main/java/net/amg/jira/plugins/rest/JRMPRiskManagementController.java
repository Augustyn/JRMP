package net.amg.jira.plugins.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.Query;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.gson.Gson;
import net.amg.jira.plugins.velocity.MatrixGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller used for validations and other useful things
 *
 * @author Adam Król
 */
@Path("/controller")
@Controller
public class JRMPRiskManagementController {

    Logger logger = LoggerFactory.getLogger(getClass());

    private I18nResolver i18nResolver;

    private SearchService searchService;

    private JiraAuthenticationContext authenticationContext;


    private MatrixGenerator matrixGenerator;

    public void setMatrixGenerator(MatrixGenerator matrixGenerator) {
        this.matrixGenerator = matrixGenerator;
    }

    public void setI18nResolver(I18nResolver i18nResolver) {
        this.i18nResolver = i18nResolver;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setAuthenticationContext(JiraAuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Path("/validate")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response doValidation(@Context HttpServletRequest request) {
        ErrorCollection errorCollection = new ErrorCollection();
        errorCollection.setParameters(request.getParameterMap());
        String filter = request.getParameter(GadgetFieldEnum.FILTER.toString());
        String relativeDate = request.getParameter(GadgetFieldEnum.DATE.toString());
        String title = request.getParameter(GadgetFieldEnum.TITLE.toString());
        String refreshRate = request.getParameter(GadgetFieldEnum.REFRESH.toString());
        Gson gson = new Gson();
        Query query = null;
        if(StringUtils.isBlank(filter))
        {

            errorCollection.addError(GadgetFieldEnum.FILTER.toString(),i18nResolver.getText("risk.management.validation.error.empty_filter"));
        }else {
            String type = filter.split("-")[0];
            if ("filter".equals(type)) {
                query = getQueryFilter(filter.split("-")[1]);
            }
            if("project".equals(type))
            {
                query = getQueryProject(filter.split("-")[1]);
            }

                if (query != null) {

                    MessageSet messageSet = searchService.validateQuery(authenticationContext.getUser().getDirectoryUser(), query);
                    if (messageSet.hasAnyErrors()) {
                        errorCollection.addError(GadgetFieldEnum.FILTER.toString(), i18nResolver.getText("risk.management.validation.error.filter_is_incorrect"));
                    }
                } else {
                    errorCollection.addError(GadgetFieldEnum.FILTER.toString(), i18nResolver.getText("risk.management.validation.error.filter_is_incorrect"));
                }

        }

        if(StringUtils.isBlank(relativeDate))
        {
            errorCollection.addError(GadgetFieldEnum.DATE.toString(), i18nResolver.getText("risk.management.validation.error.empty_date"));
        }

        if(StringUtils.isBlank(refreshRate))
        {
                errorCollection.addError(GadgetFieldEnum.REFRESH.toString(), i18nResolver.getText("risk.management.validation.error.empty_refresh"));
        }


        if(errorCollection.hasAnyErrors()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(errorCollection)).build();
        }
        return Response.ok(query.getQueryString()).build();
    }

    @Path("/matrix")
    @GET
    @Produces({MediaType.TEXT_HTML})
    public Response getMatrix(@DefaultValue("0") @QueryParam("size") int size, @QueryParam("query") String queryString) {
        if(queryString == null || queryString.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Query query = searchService.parseQuery(authenticationContext.getUser().getDirectoryUser(), queryString.replaceAll("%3D","=")).getQuery();
//        MatrixGenerator matrixGenerator = new MatrixGenerator(i18nResolver, searchService, authenticationContext);
        try {
            return Response.ok(matrixGenerator.generateMatrix(size, query), MediaType.TEXT_HTML).build();
        } catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private Query getQueryFilter(String filter) {
        JqlClauseBuilder subjectBuilder = JqlQueryBuilder.newClauseBuilder().savedFilter(filter);
        return subjectBuilder.buildQuery();
    }

    private Query getQueryProject(String project)
    {
        JqlClauseBuilder subjectBuilder = JqlQueryBuilder.newClauseBuilder().project(project);
        return subjectBuilder.buildQuery();
    }

}
