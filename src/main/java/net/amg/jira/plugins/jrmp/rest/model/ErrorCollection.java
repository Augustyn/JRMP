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

import com.atlassian.jira.rest.api.util.ValidationError;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Król
 */
@Data
public class ErrorCollection {
    private Collection<String> errorMessages = new ArrayList<>(1);
    private Collection<ValidationError> errors = new ArrayList<>(7);
    private Map<String,String> parameters = new HashMap<>(4);

    public void addError(String field, String errorMsg) {
        errors.add(new ValidationError(field, errorMsg));
    }
    public void addError(String field, String errorMsg, String param) {
        errors.add(new ValidationError(field, errorMsg, param));
    }

    public boolean hasAnyErrors() {
        return !errors.isEmpty() || !errorMessages.isEmpty();
    }

    public void addErrorMessage(String errorMsg) {
        errorMessages.add(errorMsg);
    }

    public boolean hasErrorForField(String fieldId) {
        if (errors.isEmpty()) {
            return false;
        } else {
            for (ValidationError error : errors) {
                if (error.getField().equals(fieldId))
                    return true;
            }
        }
        return false;
    }
}
