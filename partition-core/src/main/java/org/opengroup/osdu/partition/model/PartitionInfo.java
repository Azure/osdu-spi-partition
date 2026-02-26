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

package org.opengroup.osdu.partition.model;

import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Partition properties and their values",
        example = "OrderedMap { \"properties\": OrderedMap { \"compliance-ruleset\": OrderedMap { \"sensitive\": false, \"value\": \"shared\" }, \"elastic-endpoint\": OrderedMap { \"sensitive\": true, \"value\": \"elastic-endpoint\" }, \"cosmos-connection\": OrderedMap { \"sensitive\": true, \"value\": \"cosmos-connection\" } } }")
public class PartitionInfo {

    @NotEmpty
    @Builder.Default
    @Schema(description = "Free form key value pair object for any data partition specific values")
    Map<String, Property> properties = new HashMap<>();
}
