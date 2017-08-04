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

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
<%--@elvariable id="scriptName" type="java.lang.String>"--%>
<c:set var="statusType" value="${healthStatusItem.additionalData['statusType']}"/>

<c:choose>
    <c:when test="${statusType eq 'BUILD_RUNNER'}">
        <c:set var="stepIndex" value="2"/>
    </c:when>
    <c:otherwise>
        <c:set var="stepIndex" value="5"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty buildType}">
        <c:set var="target" value="${buildType}"/>
        <div>
            <%--@elvariable id="buildConfigSteps" type="java.util.ArrayList<jetbrains.buildServer.controllers.admin.projects.ConfigurationStep>"--%>
            <%--@elvariable id="pageUrl" type="java.lang.String"--%>
            <%--@elvariable id="target" type="jetbrains.buildServer.serverSide.SBuildType"--%>
            <admin:editBuildTypeNavSteps settings="${target}"/>
            <admin:editBuildTypeLink buildTypeId="${target.externalId}"
                                     step="${buildConfigSteps[stepIndex].stepId}"
                                     cameFromUrl="${pageUrl}">
                <bs:out value="${target.fullName}"/>
            </admin:editBuildTypeLink>
            &nbsp;references Gradle init script <strong><bs:out value="${scriptName}"/></strong> that is missing
        </div>
    </c:when>
    <c:when test="${not empty buildTemplate}">
        <c:set var="target" value="${buildTemplate}"/>
        <div>
            <%--@elvariable id="buildConfigSteps" type="java.util.ArrayList<jetbrains.buildServer.controllers.admin.projects.ConfigurationStep>"--%>
            <%--@elvariable id="pageUrl" type="java.lang.String"--%>
            <%--@elvariable id="target" type="jetbrains.buildServer.serverSide.BuildTypeTemplate"--%>
            <admin:editBuildTypeNavSteps settings="${target}"/>
            <admin:editTemplateLink templateId="${target.externalId}"
                                    step="${buildConfigSteps[stepIndex].stepId}"
                                    cameFromUrl="${pageUrl}">
                <bs:out value="${target.fullName}"/>
            </admin:editTemplateLink>
            &nbsp;references Gradle init script <strong><bs:out value="${scriptName}"/></strong> that is missing
        </div>
    </c:when>
</c:choose>
