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

package org.opengroup.osdu.partition.provider.azure.utils;

public class TestUtils {
    private static final String appId = "1234";
    public static final String APPID = "appid";
    public static final String aadIssuer = "https://sts.windows.net";
    public static final String aadIssuerV2 = "https://login.microsoftonline.com";
    public static final String nonAadIssuer = "https://login.abc.com";

    public static String getAppId() {return appId;}
    public static String getAadIssuer() {return aadIssuer;}
    public static String getAadIssuerV2() {return aadIssuerV2;}
    public static String getNonAadIssuer() {return nonAadIssuer;}
}
