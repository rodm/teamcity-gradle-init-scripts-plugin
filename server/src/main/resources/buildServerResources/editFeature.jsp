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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.BuildTypeForm" scope="request"/>

<c:set var="INIT_SCRIPT_NAME" value="<%=GradleInitScriptsPlugin.INIT_SCRIPT_NAME%>"/>

<tr>
    <td colspan="2"><em>This build feature runs Gradle build steps with an initialization script during a build.</em></td>
</tr>
<tr>
    <th>
        <label for="${INIT_SCRIPT_NAME}">Gradle initialization script:</label>
    </th>
    <td>
        <c:set var="selectedScript" value="${propertiesBean.properties[INIT_SCRIPT_NAME]}" scope="request"/>
        <jsp:include page="/admin/initScripts.html?projectId=${buildForm.project.projectId}"/>
        <span class="error" id="error_${INIT_SCRIPT_NAME}"></span>
    </td>
</tr>
