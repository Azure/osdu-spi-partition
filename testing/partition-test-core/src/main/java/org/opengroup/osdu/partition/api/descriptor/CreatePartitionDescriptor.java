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

package org.opengroup.osdu.partition.api.descriptor;

import org.opengroup.osdu.partition.util.RestDescriptor;
import org.springframework.web.bind.annotation.RequestMethod;

public class CreatePartitionDescriptor extends RestDescriptor {

    private String partitionId;

    @Override
    public String getPath() {
        return "api/partition/v1/partitions/" + this.arg();
    }

    @Override
    public String getHttpMethod() {
        return RequestMethod.POST.toString();
    }

    @Override
    public String getValidBody() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        sb.append("  \"properties\": {")
                .append("\"elasticPassword\": {\"sensitive\":true,\"value\":\"test-password\"},")
                .append("\"serviceBusConnection\": {\"sensitive\":true,\"value\":\"test-service-bus-connection\"},")
                .append("\"complianceRuleSet\": {\"value\":\"shared\"}")
                .append("}\n")
                .append("}");
        return sb.toString();
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }
}
