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

package org.opengroup.osdu.model.exception;

import lombok.Getter;

public class AppException extends RuntimeException {
  @Getter private final AppError error;
  @Getter private final Exception originalException;

  public AppException(int status, String reason, String message) {
    this(status, reason, message, null);
  }

  public AppException(int status, String reason, String message, Exception originalException) {
    super(sanitizeString(message), originalException);
    String sanitizedReason = sanitizeString(reason);
    this.error = new AppError(status, sanitizedReason, this.getMessage());
    this.originalException = originalException;
  }

  private static String sanitizeString(String msg) {
    if (msg == null || msg.isEmpty()) {
      return "";
    }
    return msg.replace('\n', '_').replace('\r', '_');
  }
}
