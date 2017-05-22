
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Set" %>
<%@ page import="jetbrains.buildServer.util.StringUtil" %>
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
<%--@elvariable id="errors" type="java.util.Set<java.lang.String>"--%>
<c:set var="errors" value="${healthStatusItem.additionalData['errors']}"/>

<c:choose>
    <c:when test="${not empty buildType}">
        <c:set var="target" value="${buildType}"/>
        <div>
            <%--@elvariable id="buildConfigSteps" type="java.util.ArrayList<jetbrains.buildServer.controllers.admin.projects.ConfigurationStep>"--%>
            <%--@elvariable id="pageUrl" type="java.lang.String"--%>
            <%--@elvariable id="target" type="jetbrains.buildServer.serverSide.SBuildType"--%>
            <admin:editBuildTypeNavSteps settings="${target}"/>
            <admin:editBuildTypeLink buildTypeId="${target.externalId}"
                                     step="${buildConfigSteps[5].stepId}"
                                     cameFromUrl="${pageUrl}">
                <bs:out value="${target.fullName}"/>
            </admin:editBuildTypeLink>
            &nbsp;contains <strong><bs:out value="${fn:length(errors)}"/></strong> unresolved Gradle init script
            name<bs:s val="${fn:length(errors)}"/>:
            <%
                @SuppressWarnings("unchecked")
                final Set<String> set = (Set<String>)pageContext.getAttribute("errors");
                if (set != null) {
            %>
            <%=StringUtil.join(set, ", ")%>
            <%
                }
            %>
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
                                    step="${buildConfigSteps[5].stepId}"
                                    cameFromUrl="${pageUrl}">
                <bs:out value="${target.fullName}"/>
            </admin:editTemplateLink>
            &nbsp;contains <strong><bs:out value="${fn:length(errors)}"/></strong> unresolved Gradle init script
            name<bs:s val="${fn:length(errors)}"/>:
            <%
                @SuppressWarnings("unchecked")
                final Set<String> set = (Set<String>)pageContext.getAttribute("errors");
                if (set != null) {
            %>
            <%=StringUtil.join(set, ", ")%>
            <%
                }
            %>
        </div>
    </c:when>
</c:choose>
