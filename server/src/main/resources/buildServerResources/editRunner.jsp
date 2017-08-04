<%--
  * Copyright 2017 Rod MacKenzie.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  --%>

<%@ include file="/include-internal.jsp"%>
<%@ page import="com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin" %>

<c:set var="INIT_SCRIPT_NAME_PARAMETER" value="<%=GradleInitScriptsPlugin.INIT_SCRIPT_NAME_PARAMETER%>"/>

<l:settingsGroup title="Gradle Init Scripts" className="advancedSetting">
    <tr class="advancedSetting">
    <th class="noBorder"><label for="${INIT_SCRIPT_NAME_PARAMETER}">Initialization script:</label></th>
    <td class="noBorder">
        <%--@elvariable id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean"--%>
        <%--@elvariable id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm."--%>
        <c:set var="selectedScript" value="${propertiesBean.properties[INIT_SCRIPT_NAME_PARAMETER]}" scope="request"/>
        <jsp:include page="/admin/initScripts.html?projectId=${buildForm.project.projectId}&chooserName=${INIT_SCRIPT_NAME_PARAMETER}"/>
        <span class="smallNote">Select one of the predefined scripts.</span>
        <span>
            <admin:editProjectLink projectId="${buildForm.project.externalId}" addToUrl="&tab=gradleInitScripts">
                Manage init scripts
            </admin:editProjectLink>
        </span>
    </td>
    </tr>
</l:settingsGroup>
