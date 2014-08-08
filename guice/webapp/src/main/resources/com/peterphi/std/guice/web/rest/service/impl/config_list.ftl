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

    <p>The following configuration properties are in use by this webapp. This may grow as more services are dynamically loaded.</p>

	<ul>
	<#list configRegistry.getAll() as prop>
		<li><a href="#${prop.name?html}">${prop.name}</a></li>
		</#list>
	</ul>

	<h1>Detail</h1>

    <#list configRegistry.getAll() as prop>
    <div class="property-container" data-property-name="${prop.name?html}">
	    <h3>
	        <a href="#${prop.name?html}" id="${prop.name}">${prop.name}</a>
	    </h3>

		<#if prop.deprecated>
	        <div class="alert">
	            <strong>Deprecated</strong><br/>
	            This property is included for legacy use and will be removed in a future release
	        </div>
		</#if>

	    <p data-content-meaning="documentation">${prop.documentation!""?html?replace('\n', '<br/>')}</p>

		<#if allowReconfigure>
		<h5>Change</h5>
		<p>
		<form action="${urls.rest("/list/config/reconfigure")}" method="POST">
		<input type="hidden" name="key" value="${prop.name}" />
		<input type="text" name="value"
			<#if showProperties>
			value="${config.getString(prop.name,"")}"
			</#if>
		/ >
		<input type="submit" value="Change" />
		</form>
		</p>
		</#if>

	    <h5>Binding Information</h5>
	    <table class="table">
	        <tbody>
	        <#if showProperties>
	        <tr>
	            <th class="span3">Configured Value</th>
	            <td data-content-meaning="liveValue">${config.getString(prop.name, "Not configured - using internal default")}</td>
	        </tr>
	        </#if>
	        <tr>
		        <th class="span3">Data type</th>
		        <td data-content-meaning="propertyType">${prop.type!"Unknown"}</td>
	        </tr>
			<#if prop.hrefs?has_content>
	        <tr>
	            <th class="span3">See Also</th>
	            <td>
	                <ul>
						<#list prop.hrefs as href>
	                        <li data-content-meaning="docHref"><a href="${href?html}">${href?html}</a></li>
						</#list>
	                </ul>
	            </td>
	        </tr>
			</#if>
			<#if prop.bindings?has_content>
	        <tr>
	            <th class="span3">Binding Sites</th>
	            <td>
	                <ul>
						<#list prop.bindings as binding>
	                        <li data-content-meaning="bindingClass">${binding.owner?html}</li>
						</#list>
	                </ul>
	            </td>
	        </tr>
			</#if>
	        </tbody>
	    </table>
    </div>
    </#list>
</div>
</body>
</html>
