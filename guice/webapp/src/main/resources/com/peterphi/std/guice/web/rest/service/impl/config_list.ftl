<#-- @ftlvariable name="urls" type="com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerURLHelper" -->
<#-- @ftlvariable name="bootstrap" type="com.peterphi.std.guice.web.rest.util.BootstrapStaticResources" -->
<#-- @ftlvariable name="configRegistry" type="com.peterphi.std.guice.common.serviceprops.ConfigurationPropertyRegistry" -->
<#-- @ftlvariable name="config" type="org.apache.commons.configuration.Configuration" -->
<#-- @ftlvariable name="showProperties" type="boolean" -->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Service Configuration</title>
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
    <h1>Configuration</h1>

    <p>The following configuration values are in use by this webapp.</p>
    <table class="table">
        <thead>
        <tr>
	        <th>Type</th>
	        <th>Name</th>
	        <#if showProperties>
	        <th>Live Value</th>
	        </#if>
            <th>Description</th>
        </tr>
        </thead>
        <tbody>
		<#list configRegistry.getAll() as prop>
        <tr>
	        <td>${prop.type!"(Unknown Type)"}</td>
	        <td>${prop.name}</td>
			<#if showProperties>
            <td>${config.getString(prop.name, "(Unknown Internal Default Value)")}</td>
			</#if>
            <td>${prop.documentation!""}</td>
        </tr>
		</#list>
        </tbody>
    </table>
</div>
</body>
</html>
