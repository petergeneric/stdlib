<#-- @ftlvariable name="urls" type="com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerURLHelper" -->
<#-- @ftlvariable name="bootstrap" type="com.peterphi.std.guice.web.rest.util.BootstrapStaticResources" -->
<#-- @ftlvariable name="services" type="java.util.List<com.peterphi.std.guice.web.rest.service.servicedescription.freemarker.RestServiceInfo>" -->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>REST resources</title>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>

    <style>
${bootstrap.CSS}
	</style>

    <style>
        pre {
            font-size: 11px;
        }

        body {
            padding-top: 60px;
            padding-bottom: 40px;
        }

        .dl-horizontal > dd:after {
            display: table;
            content: "";
            clear: both;
        }
    </style>
</head>

<body id="top">
<div class="container">
    <h1>REST Services</h1>

    <p>The REST services registered within this webapp.</p>
    <table class="table">
        <thead>
        <tr>
            <th>Path</th>
            <th>Name</th>
            <th>Description</th>
        </tr>
        </thead>
        <tbody>
		<#list services as service>
        <tr>
            <td>${service.path}</td>
            <td><strong><a href="${urls.rest("/list/service/" + service_index)}">${service.interfaceName}</a></strong></td>
            <td>${service.description}</td>
        </tr>
		</#list>
        </tbody>
    </table>
</div>
</body>
</html>
