// Copyright 2017-2020, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.provider.azure.persistence;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableTransactionAction;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.validation.ValidationException;

@Component
public class DataTableStore {
    @Autowired
    private TableClient tableClient;
    private final String WHITELISTED_CHARACTERS = "[-_[A-Za-z0-9]]";
    public boolean deleteCloudTableEntity(String partitionKey, String rowKey) {

        try {
            TableEntity tableEntity = new TableEntity(partitionKey, rowKey);
            tableClient.deleteEntity(tableEntity);
            return true;
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error querying cloud table", e.getMessage(), e);
        }
    }

    public Iterable<TableEntity> queryByKey(final String key, final String value) {
        /*** Addressing review comment: "We can add a validator for checking sql injection"
         * There was no strict validation of 'value' in the upper layers,
         * So, placing here a validation for allowed characters( alphanumeric, - and _)
         */
        if(checkValidString(value) == false){
            throw new ValidationException("Invalid input parameters, value contains illegal character(s)");
        }
        String filter = key + " eq '" + value + "'";
        //Create a filter condition key eq value
        ListEntitiesOptions listEntitiesOptions = new ListEntitiesOptions().setFilter(filter);
        try {
            return this.tableClient.listEntities(listEntitiesOptions, null, null);
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "error listing entities by filter: " + filter, e.getMessage(), e);
        }
    }

    public Iterable<TableEntity> queryByCompoundKey(final String rowKey, final String rowValue,
                                                              final String valueKey, final String value) {
        if(checkValidString(rowValue) == false || checkValidString(value) == false){
            throw new ValidationException("Invalid input parameters, value contains illegal character(s)");
        }

        String rowFilter = rowKey + " eq '" + rowValue + "'";

        String valueFilter = valueKey + " eq '" + value + "'";

        String combinedFilter = rowFilter + " and " + valueFilter;
        ListEntitiesOptions listEntitiesOptions = new ListEntitiesOptions().setFilter(combinedFilter);

        try {
            return tableClient.listEntities(listEntitiesOptions, null, null);
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error getting partition", e.getMessage(), e);
        }
    }

    public void insertBatchEntities(List<TableTransactionAction> transactionAction) {
        try {
            this.tableClient.submitTransaction(transactionAction);
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error creating partition", e.getMessage(), e);
        }
    }

    /**
     * Helper function to check if the input string contains only the allowed characters
     * Alphanumeric characters and - and underscore...
     * We match the length of input string and the length of characters matched from the allowed list
     * */
    private Boolean checkValidString(String input){
        Pattern pattern = Pattern.compile(WHITELISTED_CHARACTERS);
        Matcher matcher = pattern.matcher(input);
        int matchedCharacterCount = 0;
        while (matcher.find()) {
            matchedCharacterCount++;
        }
        return matchedCharacterCount == input.length();
    }
}
