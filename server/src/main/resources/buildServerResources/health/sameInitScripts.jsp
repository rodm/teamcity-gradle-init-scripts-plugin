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

<%--@elvariable id="buildType" type="jetbrains.buildServer.serverSide.SBuildType"--%>
<c:set var="buildType" value="${healthStatusItem.additionalData['buildType']}"/>
<%--@elvariable id="buildTypeTemplate" type="jetbrains.buildServer.serverSide.BuildTypeTemplate"--%>
<c:set var="buildTemplate" value="${healthStatusItem.additionalData['buildTemplate']}"/>
<%--@elvariable id="scriptName" type="java.lang.String>"--%>
<c:set var="scriptName" value="${healthStatusItem.additionalData['scriptName']}"/>

<c:choose>
    <%--@elvariable id="buildConfigSteps" type="java.util.ArrayList<jetbrains.buildServer.controllers.admin.projects.ConfigurationStep>"--%>
    <%--@elvariable id="pageUrl" type="java.lang.String"--%>
    <c:when test="${not empty buildType}">
        <div>
            <admin:editBuildTypeNavSteps settings="${buildType}"/>
            <admin:editBuildTypeLink buildTypeId="${buildType.externalId}"
                                     step="${buildConfigSteps[5].stepId}"
                                     cameFromUrl="${pageUrl}">
                <bs:out value="${buildType.fullName}"/>
            </admin:editBuildTypeLink>
            &nbsp;has the same Gradle init script, <strong><bs:out value="${scriptName}"/></strong>, configured for a build runner and for a build feature
        </div>
    </c:when>
    <c:when test="${not empty buildTemplate}">
        <div>
            <admin:editBuildTypeNavSteps settings="${buildTemplate}"/>
            <admin:editTemplateLink templateId="${buildTemplate.externalId}"
                                    step="${buildConfigSteps[5].stepId}"
                                    cameFromUrl="${pageUrl}">
                <bs:out value="${buildTemplate.fullName}"/>
            </admin:editTemplateLink>
            &nbsp;has the same Gradle init script, <strong><bs:out value="${scriptName}"/></strong>, configured for a build runner and for a build feature
        </div>
    </c:when>
</c:choose>
