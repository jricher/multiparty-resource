<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<table class="table table-striped">
	<thead>
		<tr>
			<th>Label</th>
			<th>Value</th>
			<th></th>
	</thead>
	<tbody>


<security:authorize access="hasRole('ROLE_USER')">

		<form action="create" method="POST">
		<tr>
			<td>
				<input type="text" name="label" placeholder="label" />
			</td>
			<td>
				<input type="text" name="value" placeholder="value" />
			</td>
			<td>
				<input type="submit" value="Add" class="btn btn-success" />
			</td>
		</tr>
		</form>

</security:authorize>

		
		<c:forEach items="${ resources }" var="res">
		<tr>
			<td><c:out value="${ res.label }" /></td>
			<td><c:out value="${ res.value }" /></td>
			<td><a href="http://localhost:8080/multiparty-resource/api/<c:out value="${ res.id  }" />"><c:out value="${ res.id  }" /></a></td>
		</tr>	
		</c:forEach>
	</tbody>
</table>

<div class="well">

<c:if test="${ not empty sharedResourceSet }">

<div class="alert alert-success">

<b>Shared</b>

This resource has been shared on server <c:out value="${ sharedResourceSet.issuer }" /> with ID <code><c:out value="${ sharedResourceSet.rsid }" /></code>.
<a href="<c:out value="${ sharedResourceSet.userAccessPolicyUri }" />">Manage policies for this resource.</a>

<form action="unshare" method="POST">
	<input type="submit" value="Un-share" class="btn" />
</form>
</div>

</c:if>

<security:authorize access="hasRole('ROLE_USER')">

<form action="share" method="POST">

<input type="text" value="${ issuer }" name="issuer" />

<input type="submit" value="${ not empty sharedResourceSet ? "Re-share" : "Share" }" class="btn btn-default" />

</form>

</security:authorize>


</div>