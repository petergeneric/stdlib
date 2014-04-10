<#-- @ftlvariable name="urls" type="com.peterphi.std.guice.web.rest.templating.freemarker.FreemarkerURLHelper" -->
<#-- @ftlvariable name="schemaGenerator" type="com.peterphi.std.guice.web.rest.service.servicedescription.freemarker.SchemaGenerateUtil" -->
<#-- @ftlvariable name="bootstrap" type="com.peterphi.std.guice.web.rest.util.BootstrapStaticResources" -->
<#-- @ftlvariable name="service" type="com.peterphi.std.guice.web.rest.service.servicedescription.freemarker.RestServiceInfo" -->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>${service.interfaceName}</title>
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
    <h1>${service.interfaceName}</h1>

<#if service.deprecated>
    <div class="alert">
        <strong>Deprecated</strong><br/>
        This service is included for legacy use and will be removed in a future release
    </div>
</#if>

    <p>${service.description?html?replace('\n', '<br/>')}</p>

<#if service.seeAlsoURLs?has_content>
    See also:
    <ul>
		<#list service.seeAlsoURLs as seeAlsoURL>
            <li><a href="${seeAlsoURL?html}">${seeAlsoURL?html}</a></li>
		</#list>
    </ul>
</#if>


    <h2>Resources</h2>
    <ol>
	<#list service.resources as resource>
        <li><a href="#resource_${resource_index+1}">${resource.localPath} (${resource.httpMethod})</a></li>
	</#list>
    </ol>


<#list service.resources as resource>
    <h3>
        <a href="#resource_${resource_index+1}" id="resource_${resource_index+1}">${resource_index+1}.
		${resource.httpMethod} ${resource.localPath}</a>
    </h3>

	<#if resource.deprecated>
        <div class="alert">
            <strong>Deprecated</strong><br/>
            This resource is included for legacy use and will be removed in a future release
        </div>
	</#if>

    <p>${resource.description?html?replace('\n', '<br/>')}</p>

    <h5>Details</h5>
    <table class="table">
        <tbody>
        <tr>
            <th>HTTP Verb</th>
            <td>${resource.httpMethod}</td>
        </tr>
        <tr>
	        <th>Full Path</th>
	        <#if resource.httpMethod != "GET">
		        <td>${urls.restConcat(resource.path)?html}</td>
	        <#elseif resource.path?contains("{")>
		        <td>${urls.restConcat(resource.path)?html}</td>
	        <#else>
		        <td><a href="${urls.restConcat(resource.path)?html}">${urls.restConcat(resource.path)?html}</a></td>
	        </#if>
        </tr>
        <tr>
            <th>Local Path</th>
            <td>${resource.path?html}</td>
        </tr>
        <tr>
            <th>Accept</th>
            <td>${resource.consumes}</td>
        </tr>
        <tr>
            <th>Content-Type</th>
            <td>${resource.produces}</td>
        </tr>
        <tr>
            <th>Java Method</th>
            <td>${resource.methodString}</td>
        </tr>

			<#if resource.seeAlsoURLs?has_content>
            <tr>
                <th>See Also</th>
                <td>
                    <ul>
						<#list resource.seeAlsoURLs as seeAlsoURL>
                            <li><a href="${seeAlsoURL?html}">${seeAlsoURL?html}</a></li>
						</#list>
                    </ul>
                </td>
            </tr>
			</#if>
        </tbody>
    </table>

    <h5>Parameter Guide</h5>
	<#if resource.parameters?has_content>
        <table class="table">
            <thead>
            <tr>
                <th>Name</th>
                <th>Type</th>
                <th>Description</th>
            </tr>
            </thead>
            <tbody>
				<#list resource.parameters as param>
                <tr>
                    <td>${param.name?html}</td>
                    <td>${param.type?html}</td>
                    <td>${param.description?html?replace('\n', '<br/>')}
						<#if param.seeAlsoURLs?has_content>
							(See <#list param.seeAlsoURLs as seeAlsoURL>
								<sup><a href="${seeAlsoURL?html}">${seeAlsoURL_index}</a> </sup>
							</#list>)
						</#if>

						<#if param.defaultValue?has_content>
                            <p>Default Value: ${param.defaultValue?html}</p>
						</#if>
                    </td>
                </tr>
				</#list>
            </tbody>
        </table>
	<#else>
        <p><em>No parameters</em></p>
	</#if>

	<#if resource.requestEntity?has_content>
	<h5>Request Schema</h5>
    <pre>${schemaGenerator.getSchema(resource.requestEntity.dataType)?html}</pre>
	</#if>

    <h5>Response Schema</h5>
    <pre>${schemaGenerator.getSchema(resource.returnType)?html}</pre>

    <h5>curl template</h5>
    <code>curl -X ${resource.httpMethod} "${urls.restConcat(resource.path)}"
	<#if resource.requestEntity?has_content>
		<#if resource.httpMethod != "GET" >
            --data-binary @file.xml
		</#if>
	</#if>
    </code>
    <br/>
    <hr/>
    <br/>
</#list>
</div>
</body>
</html>
