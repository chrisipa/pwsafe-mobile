PasswordSafe = (function () {
	
	function init() {
		
		$(document).ready(function () {
			
			$(document).ajaxStart(function() {
				$.mobile.loading('show');
			});
			
			$(document).ajaxStop(function() {
				$.mobile.loading('hide');
			});
			
			$(document).on("pagechange", function(event) {
				$("input[type=text]:visible").first().focus();
			});
			
			$.i18n.properties({
				name: 'translations', 
				path: Settings.bundlePath, 
				mode: 'both',
				language: $.i18n.browserLang(),
				callback: function () {
					showCurrentPage();
				}
			});
		});
	}
	
	function showCurrentPage() {
		if (Settings.allowed) {
			showSearchPage();
		}
		else {
			showLoginPage();
		}
	}
	
	function showLoginPage() {
		
		var loginPage = $("<div id='loginPage' data-role='page'>");
		
		$.mobile.pageContainer.html(loginPage);
		
		var header = $("<div data-role='header'></div>"); 
		
		loginPage.append(header);
		
		var headline = $("<h1></h1>").html(Settings.name + " (" + Settings.version + ")");
		
		header = header.append(headline);
		
		var content = $("<div data-role='content'></div>");
		
		header.after(content);
		
		var loginForm = $("<form id='login'></form>");
		loginForm.append($("<input type='text' id='username' />").attr("Placeholder", $.i18n.prop("input.username.label")));
		loginForm.append($("<input type='password' id='password' />").attr("Placeholder", $.i18n.prop("input.password.label")));
		
		var code = $("<input type='text' id='code' />").attr("Placeholder", $.i18n.prop("input.code.label"));
		if (!Settings.googleAuthEnabled) {
			code.hide();
		}
		
		loginForm.append(code);
		loginForm.append($("<input type='submit' />").attr("value", $.i18n.prop("button.login.label")));
		loginForm.submit(function (e) {

			e.preventDefault();

			$.ajax({
				type: "POST",
				contentType: "application/x-www-form-urlencoded; charset=utf-8",
				url: "login",
				data: {
					"username": $("#username").val(),
					"password": $("#password").val(),
					"code": $("#code").val()
				},
				success: function (response) {
					Settings.username = $("#username").val();
					showSearchPage();
				},
				error: function(request,status,errorThrown) {
					alert($("<div />").html($.i18n.prop("login.error.message")).text());
				} 	
			});
		});
		
		content.append(loginForm);
		
		$.mobile.changePage(loginPage);
	}

	function showSearchPage() {
		
		var searchPage = $("<div id='searchPage' data-role='page'>");
		
		$.mobile.pageContainer.html(searchPage);
		
		var header = $("<div data-role='header'></div>"); 
		
		searchPage.append(header);
		
		var headline = $("<h1></h1>").html(Settings.name + " (" + Settings.version + ")");
		
		header = header.append(headline);
		
		var addButton = $("<a href='#' id='add' class='ui-btn-left ui-btn ui-btn-inline ui-mini ui-corner-all ui-btn-icon-left ui-icon-plus'></a>");
		addButton.html($.i18n.prop("button.add.label"));
		addButton.click(function (e) {
			
			e.preventDefault();
			
			$("#list").html("");
			
			var password = {
				"id" : "new-entry",	
				"title" : $.i18n.prop("passwort.create.dummy.label"),	
				"notes" : "",	
				"username" : ""
			};
			
			addPasswordEntry(password);
			
			$("#list").trigger("create");
			$("#list").show();
			
			$("#title-new-entry").focus();
			$("#showhide").addClass('ui-disabled');
			$("#deactivate").addClass('ui-disabled');
		});
		
		headline.after(addButton);
		
		var logoutButton = $("<a href='#' id='logout' class='ui-btn-right ui-btn ui-btn-inline ui-mini ui-corner-all ui-btn-icon-left ui-icon-forward'></a>");
		logoutButton.html(Settings.username);
		logoutButton.click(function (e) {

			e.preventDefault();

			$.ajax({
				type: "GET",
				contentType: "application/x-www-form-urlencoded; charset=utf-8",
				url: "logout",
				success: function (response) {
					showLoginPage();
				},
				error: function(request,status,errorThrown) {
					alert($("<div />").html($.i18n.prop("logout.error.message")).text());
				} 	
			});
		});
		
		addButton.after(logoutButton);			
		
		var content = $("<div data-role='content'></div>");
		
		header.after(content);
		
		var searchForm = $("<form id='search'></form>");
		searchForm.append($("<input type='search' id='keyword' />").attr("Placeholder", $.i18n.prop("input.keyword.label")));
		searchForm.append($("<input type='submit' />").attr("value", $.i18n.prop("button.search.label")));
		searchForm.submit(function (e) {

			e.preventDefault();

			$.getJSON(Settings.rootUrl + "/passwords?query=" + encodeURI($("#keyword").val()), function(data) {

				$("#list").html("");

				$.each(data.passwordList, function(i, password) {
					addPasswordEntry(password);
				});

				$("#list").trigger("create");
				$("#list").show();
			});
		});
		
		content.append(searchForm);
		
		var listForm = $("<form id='list'></form>");
		
		content.append(listForm);		
		
		$.mobile.changePage(searchPage);
	}	
	
	function rebindTextChange($input) {
		$input.unbind("textchange").bind("textchange", function() {
			$(this).addClass("edited");
		});
	}
	
	function addPasswordEntry(password) {
		
		var passwordEntry = $("<div data-role='collapsible'></div>");
		
		passwordEntry.on("collapsiblecollapse", function(event, ui) {
			rebindTextChange($(this).find(".editable"));
		});
		
		if (password.id == "new-entry") {
			passwordEntry.attr("data-collapsed", "false");
		}
		
		passwordEntry
		.html(
			$("<h3></h3>")
			.attr("id", "entry-" + password.id)
			.html(password.title)
		)
		.append(
			$("<label for='title'></label>").html($.i18n.prop("input.title.label"))
		)
		.append(
			$("<input type='text' name='title' />")
			.attr("id", "title-" + password.id)
			.attr("class", "editable")
			.attr("value", password.id == "new-entry" ? "" : password.title)
		)
		.append(
			$("<label for='notes'></label>").html($.i18n.prop("input.notes.label"))
		)
		.append(
			$("<textarea name='notes'></textarea>")
			.attr("id", "notes-" + password.id)
			.attr("class", "editable")
			.html(password.notes)
		)
		.append(
			$("<label for='username'></label>").html($.i18n.prop("input.username.label"))
		)
		.append(
			$("<input type='text' name='username' />")
			.attr("id", "username-" + password.id)
			.attr("class", "editable")
			.attr("value", password.username)
		)
		.append(
			$("<label for='password'></label>").html($.i18n.prop("input.password.label"))
		)
		.append(
			$("<input type='text' name='password' />")
			.attr("id", "password-" + password.id)
			.attr("class", "editable")
			.attr("value", password.id == "new-entry" ? "" : Settings.passwordDefaultValue)
		)
		.append(
			$("<a id='showhide' data-role='button' data-inline='true'></a>")
			.html($.i18n.prop("button.show.label"))
			.attr("data-id", password.id)
			.click(function(e) {
				
				e.preventDefault();
				
				var id = $(this).attr("data-id");
				var passwordValueId = "#password-" + id;
				var passwordField = $(passwordValueId);
				var passwordValue = passwordField.val(); 
				passwordField.removeClass("edited");
				
				if (passwordValue == Settings.passwordDefaultValue) {
					$.getJSON(Settings.rootUrl + "/passwords/" + id + "/currentValue", function(data) {
						passwordField.val(data.currentPassword);
						rebindTextChange(passwordField);
					});
					$(this).html($.i18n.prop("button.hide.label"));
				}
				else {
					passwordField.val(Settings.passwordDefaultValue);
					$(this).html($.i18n.prop("button.show.label"));
				}
			})
		)
		.append(
			$("<a id='generate' data-role='button' data-inline='true'></a>"
		)
		.html($.i18n.prop("button.generate.label"))
		.attr("data-id", password.id)
		.click(function(e) {

			e.preventDefault();
			
			var id = $(this).attr("data-id");
			
			$.getJSON(Settings.rootUrl + "/password/generate", function(data) {
				$("#password-" + id).val(data.generatedPassword);
				$("#password-" + id).trigger("textchange");
			});	
		}))
		.append(
			$("<hr>").attr("class", "separator")
		);
		
		if (password.id == "new-entry") {
			
			passwordEntry
			.append(
					$("<a id='create' data-role='button' data-inline='true'></a>")
					.html($.i18n.prop("button.create.label"))
					.attr("data-id", password.id)
					.click(function(e) {
						
						e.preventDefault();
						
						var id = $(this).attr("data-id");
						
						var data = {};
						data["title"] = $("#title-" + id).val();
						data["notes"] = $("#notes-" + id).val();
						data["username"] = $("#username-" + id).val();
						data["password"] = $("#password-" + id).val();
						
						$.ajax({
							url: Settings.rootUrl + "/passwords",
							type: "POST",
							contentType: "application/json; charset=utf-8",
							dataType: "json",
							processData: false,
							data: JSON.stringify(data),
							success: function(response) {
								if (response.success) {
									$("#entry-" + id).children(":first").html(data["title"]);
									$("#entry-" + id).parent().find(".edited").removeClass("edited");
									$("#generate").addClass('ui-disabled');
									$("#showhide").addClass('ui-disabled');
									$("#create").addClass('ui-disabled');
									$("#deactivate").addClass('ui-disabled');
									alert($("<div />").html($.i18n.prop("password.create.success.message")).text());
								}
								else {
									alert($("<div />").html($.i18n.prop("password.create.error.message")).text());
								}
							},
							error: function(request,status,errorThrown) {
								showServerError();
							}
						});
					})
			);
			
			passwordEntry.trigger("collapsiblecollapse");
		}
		else {
			
			passwordEntry
			.append(
				$("<a id='change' data-role='button' data-inline='true'></a>")
				.html($.i18n.prop("button.change.label"))
				.attr("data-id", password.id)
				.click(function(e) {
					
					e.preventDefault();
					
					var id = $(this).attr("data-id");
					var passwordValueId = "#password-" + id;
					var passwordValue = $(passwordValueId).val();
					
					var data = {};
					data["id"] = id;
					data["title"] = $("#title-" + id).val();
					data["notes"] = $("#notes-" + id).val();
					data["username"] = $("#username-" + id).val();
					
					if (passwordValue != Settings.passwordDefaultValue) {
						data["password"] = passwordValue;
					}
					
					$.ajax({
						url: Settings.rootUrl + "/passwords",
						type: "PUT",
						contentType: "application/json; charset=utf-8",
						dataType: "json",
						processData: false,
						data: JSON.stringify(data),
						success: function(response) {
							if (response.success) {
								$("#entry-" + id).children(":first").html(data["title"]);
								$("#entry-" + id).parent().find(".edited").removeClass("edited");
								alert($("<div />").html($.i18n.prop("password.change.success.message")).text());
							}
							else {
								alert($("<div />").html($.i18n.prop("password.change.error.message")).text());
							}
						},
						error: function(request,status,errorThrown) {
							showServerError();
						}
					});
				})
			);		
		}
		
		passwordEntry
		.append(
			$("<a id='deactivate' data-role='button' data-inline='true'></a>")
			.html($.i18n.prop("button.deactivate.label"))
			.attr("data-id", password.id)
			.click(function(e) {
				
				e.preventDefault();
				
				if(confirm($("<div />").html($.i18n.prop("password.deactivate.confirm.message")).text())) {
				
					var id = $(this).attr("data-id");
					var passwordValueId = "#password-" + id;
					var passwordValue = $(passwordValueId).val();
					
					var data = {};
					data["id"] = id;
					data["title"] = $("#title-" + id).val();
					data["notes"] = $("#notes-" + id).val();
					data["username"] = $("#username-" + id).val();
					data["active"] = "false";
					
					if (passwordValue != Settings.passwordDefaultValue) {
						data["password"] = passwordValue;
					}
					
					$.ajax({
						url: Settings.rootUrl + "/passwords",
						type: "PUT",
						contentType: "application/json; charset=utf-8",
						dataType: "json",
						processData: false,
						data: JSON.stringify(data),
						success: function(response) {
							if (response.success) {
								$("#generate").addClass('ui-disabled');
								$("#showhide").addClass('ui-disabled');
								$("#change").addClass('ui-disabled');
								$("#deactivate").addClass('ui-disabled');
								alert($("<div />").html($.i18n.prop("password.deactivate.success.message")).text());
								$("#search").submit();
							}
							else {
								alert($("<div />").html($.i18n.prop("password.deactivate.error.message")).text());
							}
						},
						error: function(request,status,errorThrown) {
							showServerError();
						}
					});
				}
			})
		);
		
		$("#list").append(passwordEntry);
	}	
	
	function showServerError() {
		alert($("<div />").html($.i18n.prop("server.error.message")).text());
	}
	
	return {
        init: init,
	}
}()).init();
