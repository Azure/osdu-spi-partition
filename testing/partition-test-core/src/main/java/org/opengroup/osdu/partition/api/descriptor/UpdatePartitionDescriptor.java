package org.opengroup.osdu.partition.api.descriptor;

import org.opengroup.osdu.partition.util.RestDescriptor;
import org.springframework.web.bind.annotation.RequestMethod;

public class UpdatePartitionDescriptor extends RestDescriptor {


    @Override
    public String getPath() {
        return "api/partition/v1/partitions/" + this.arg();
    }

    @Override
    public String getHttpMethod() {
        return RequestMethod.PATCH.toString();
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
}
