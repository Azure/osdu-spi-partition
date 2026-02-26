/*
 *  Copyright 2020-2025 Google LLC
 *  Copyright 2020-2025 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.annotations.Param;
import org.opengroup.osdu.model.Property;
import org.opengroup.osdu.model.exception.AppError;

@Path("/partitions")
@Tag(name = "partition-api", description = "Partition API")
public interface PartitionApi {
  @Operation(summary = "List all Partitions", description = "Returns all existing data partitions")
  @APIResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = String.class)))
  @APIResponse(
      responseCode = "400",
      description = "Bad Request",
      content = {@Content(schema = @Schema(implementation = AppError.class))})
  @APIResponse(
      responseCode = "500",
      description = "Internal Server Error",
      content = {@Content(schema = @Schema(implementation = AppError.class))})
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<String> list();

  @Operation(
      summary = "Get Partition Info",
      description = "Get all properties and their values for a given data partition id")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "OK",
            content = {
              @Content(
                  schema = @Schema(implementation = Map.class),
                  example =
                      """
                      {
                        "propertyName": {
                          "sensitive": false,
                          "value": "propertyValue"
                        }
                      }
                      """)
            }),
        @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content = {@Content(schema = @Schema(implementation = AppError.class))}),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = {@Content(schema = @Schema(implementation = AppError.class))}),
      })
  @GET
  @Path("/{partitionId:.+}")
  @Produces(MediaType.APPLICATION_JSON)
  Map<String, Property> get(@Param String partitionId);
}
