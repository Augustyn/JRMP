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
package net.amg.jira.plugins.jrmp.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.amg.jira.plugins.jrmp.rest.model.ErrorCollection;
import net.amg.jira.plugins.jrmp.rest.model.GadgetFieldNames;
import net.amg.jira.plugins.jrmp.rest.model.MatrixRequest;
import net.amg.jira.plugins.jrmp.services.MatrixGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller used for validations and other useful things
 *
 * @author Adam Król
 */
@Path("/controller")
@Controller
@Slf4j
public class JRMPRiskManagementController {
    @Autowired
    private I18nResolver i18nResolver;
    @Autowired
    private SearchService searchService;
    @Autowired
    private JiraAuthenticationContext authenticationContext;
    @Autowired
    private MatrixGenerator matrixGenerator;
    @Autowired
    private OfBizDelegator ofBizDelegator;
    //4spring dep.injection
    public JRMPRiskManagementController(){}


    @Path("/validate")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response doValidation(@QueryParam(GadgetFieldNames.FILTER) String filter, @QueryParam(GadgetFieldNames.DATE) String date,
                                 @QueryParam(GadgetFieldNames.REFRESH) String refresh, @QueryParam(GadgetFieldNames.TEMPLATE) String template,
                                 @QueryParam(GadgetFieldNames.TITLE) String title) {

        log.info("Validation: Method start");

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setFilter(filter);
        matrixRequest.setDate(date);
        matrixRequest.setTitle(title);
        matrixRequest.setRefreshRate(refresh);
        matrixRequest.setTemplate(template);

        Gson gson = new Gson();

        ErrorCollection errorCollection = matrixRequest.doValidation(i18nResolver,authenticationContext,searchService,ofBizDelegator);
        errorCollection.setParameters(matrixRequest.getParameters());

        if(errorCollection.hasAnyErrors()) {
            log.warn("Validation: Wrong parameters passed to Validator. Returning BAD_REQUEST");
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(errorCollection)).build();
        }
        log.info("Validation: Everything Went okay, returning OK.");
        return Response.ok().build();
    }

    @Path("/matrix")
    @POST
    @Produces({MediaType.TEXT_HTML, MediaType.TEXT_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public Response postMatrix(MatrixRequest matrixRequest) {
        log.debug("postMatrix: Method start, mode: POST");
        return processMatrix(matrixRequest);
    }

    @Path("/matrix")
    @GET
    @Produces({MediaType.TEXT_HTML, MediaType.TEXT_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.TEXT_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public Response getMatrix(@QueryParam(GadgetFieldNames.FILTER) String filter, @QueryParam(GadgetFieldNames.DATE) String date,
                              @QueryParam(GadgetFieldNames.REFRESH) String refresh, @QueryParam(GadgetFieldNames.TEMPLATE) String template,
                              @QueryParam(GadgetFieldNames.TITLE) String title) {
        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setFilter(filter);
        matrixRequest.setDate(date);
        matrixRequest.setTitle(title);
        matrixRequest.setRefreshRate(refresh);
        matrixRequest.setTemplate(template);
        log.debug("getMatrix: Method start mode: GET");
        return processMatrix(matrixRequest);
    }

    private Response processMatrix(MatrixRequest matrixRequest) {
        ErrorCollection errorCollection = matrixRequest.doValidation(i18nResolver,authenticationContext,searchService,ofBizDelegator);
        if (errorCollection.hasAnyErrors()) {
            log.warn("getMatrix: Wrong parameters passed in matrixRequest. Returning BAD_REQUEST. Errors: "
                    + errorCollection.getErrors().toString());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            return Response.ok(matrixGenerator.generateMatrix(matrixRequest.getProjectOrFilter(),
                    matrixRequest.getTitle(),
                    matrixRequest.getTemplate(),
                    matrixRequest.getDateModel()),
                    MediaType.TEXT_HTML).build();
        } catch(Exception e){
            log.error("processMatrix: The Matrix couldn't be generated because of: " + e.getMessage(),e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GET
    @Path("{args : (.*)?}")
    public Response emptyGETResponse()
    {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST
    @Path("{args : (.*)?}")
    public Response emptyPOSTResponse()
    {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @PUT
    @Path("{args : (.*)?}")
    public Response emptyPUTResponse()
    {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @DELETE
    @Path("{args : (.*)?}")
    public Response emptyDELETEResponse()
    {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

}
