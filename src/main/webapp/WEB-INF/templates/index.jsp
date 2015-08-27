<%@include file="inc/head.jsp" %>
<!DOCTYPE html>
<html>
	<head>
		<title>${name} (${version})</title>
		
		<meta charset="utf-8">
		
		<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
		
		<link rel="shortcut icon" href="<c:url value="/static/pwsafe/favicon.ico" />" type="image/x-icon" />
		
		<link rel="stylesheet" href="<c:url value="/static/jquery/jquery.mobile-1.4.2.min.css" />" />
		<link rel="stylesheet" href="<c:url value="/static/pwsafe/pwsafe.css" />" />
		
		<script src="<c:url value="/static/jquery/jquery-1.9.1.min.js" />"></script>
		<script src="<c:url value="/static/jquery/jquery.mobile-1.4.2.min.js" />"></script>
		<script src="<c:url value="/static/jquery/jquery.i18n.properties.js" />"></script>
		<script src="<c:url value="/static/jquery/jquery.textchange.min.js" />"></script>
		<script src="<c:url value="/static/pwsafe/pwsafe.js" />"></script>
		
		<script type="text/javascript">
			var Settings = { 
				rootUrl : "rest",
				passwordDefaultValue : "****",
				allowed : ${allowed},
				googleAuthEnabled : ${googleAuthEnabled},
				name : '${name}',
				version : '${version}',
				username : '${username}',
				bundlePath : '<c:url value="/static/pwsafe/bundle/" />'
			};
		</script>
	</head>
	<body>
		<%-- page is created dynamically with jquery mobile --%>
	</body>
</html>