<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ page session="false" %>
<o:header title="Home"/>
<o:topbar pageName="Home"/>
<div class="container-fluid main">
	<div class="row-fluid">
		<div class="span10 offset1">

			<h1>
				Hello world!
			</h1>
		
			<div>
				<p class="well">
					<security:authorize access="hasRole('ROLE_USER')">
						<b><span class="text-success">You are currently logged in.</span></b>
					</security:authorize>
					<security:authorize access="!hasRole('ROLE_USER')">
						<b><span class="text-error">You are <em>NOT</em> currently logged in.</span></b>			
					</security:authorize>
				</p>
			</div>
			
			<div>
				<o:resourcelist />
			</div>
		</div>
	</div>
</div>


<o:footer />