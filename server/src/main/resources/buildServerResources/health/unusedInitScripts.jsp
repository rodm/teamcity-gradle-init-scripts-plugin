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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem" scope="request"/>
<jsp:useBean id="showMode" type="jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemDisplayMode" scope="request"/>

<%--@elvariable id="project" type="jetbrains.buildServer.serverSide.SProject"--%>
<c:set var="project" value="${healthStatusItem.additionalData['project']}"/>
<%--@elvariable id="scriptName" type="java.lang.String"--%>
<c:set var="scriptName" value="${healthStatusItem.additionalData['scriptName']}"/>

<c:choose>
    <c:when test="${not empty project}">
        <div>
            Gradle init script <strong><bs:out value="${scriptName}"/></strong> in project
            <admin:editProjectLink projectId="${project.externalId}" addToUrl="&tab=gradleInitScripts">
                <bs:out value="${project.fullName}"/>
            </admin:editProjectLink>
            is unused.
        </div>
    </c:when>
</c:choose>
