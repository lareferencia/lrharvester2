<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ page session="false" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html lang="en">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=urf-8">
    
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>LA Referencia - Administración</title>

    <!-- Bootstrap Core CSS -->
    <link type="text/css" rel="stylesheet" href="<spring:url value="/static/css/bootstrap.min.css"/>" >
             
    <!-- HighLight CSS -->
    <link type="text/css" rel="stylesheet" href="<spring:url value="/static/css/jquery.highlight.css"/>" >

    <!-- Custom Fonts -->
    <link type="text/css" rel="stylesheet" href="<spring:url value="/static/css/font-awesome.min.css"/>" >
    
    <!-- Ng Table CSS -->
    <link rel="stylesheet" href="<spring:url value="/static/css/ng-table.min.css"/>" >   
    
    <!-- JQuery JS -->
    <script type="text/javascript" src="<spring:url value="/static/js/jquery-1.12.2.min.js"/>"></script>
        
    <!-- Bootstrap JS -->
    <script type="text/javascript" src="<spring:url value="/static/js/bootstrap.min.js"/>"></script>
    
    <!-- Angular js -->
	<script type="text/javascript" src="<spring:url value="/static/js/angular.min.js"/>"></script>
    <script type="text/javascript" src="<spring:url value="/static/js/angular-sanitize.min.js"/>"></script>
    <script type="text/javascript" src="<spring:url value="/static/js/angular-resource.min.js"/>"></script>
    <script type="text/javascript" src="<spring:url value="/static/js/angular-route.min.js"/>"></script>
    <script type="text/javascript" src="<spring:url value="/static/js/angular-animate.min.js"/>"></script>
    
    <!-- Libs de Angular NG Tables -->
    <script type="text/javascript" src="<spring:url value="/static/js/ng-table.min.js"/>"></script>
    
	<!-- Libs de Angular Spring Data Rest -->
	<script type="text/javascript" src="<spring:url value="/static/js/angular-spring-data-rest.js"/>"></script>
	    
    <!-- Libs Angular de forms -->
    <script type="text/javascript" src="<spring:url value="/static/js/tv4.min.js"/>"></script>
	<script type="text/javascript" src="<spring:url value="/static/js/ObjectPath.js"/>"></script>
	<script type="text/javascript" src="<spring:url value="/static/js/schema-form.min.js"/>"></script>
    <script type="text/javascript" src="<spring:url value="/static/js/bootstrap-decorator.min.js"/>"></script>
    
    <!-- LoDash -->
    <script type="text/javascript" src="<spring:url value="/static/js/lodash.core.js"/>"></script>
   
    <!-- Angular UI Bootstrap --> 
    <script type="text/javascript" src="<spring:url value="/static/js/ui-bootstrap-tpls-1.2.4.min.js"/>"></script>
   
	<!-- Controladores Angular de la aplicación -->
	<script type="text/javascript" src="<spring:url value="/static/app/model-json-schemas.js"/>"></script>
	<script type="text/javascript" src="<spring:url value="/static/app/table-services.js"/>"></script>
	<script type="text/javascript" src="<spring:url value="/static/app/rest-url-helper.js"/>"></script>
	<script type="text/javascript" src="<spring:url value="/static/app/data-services.js"/>"></script>
	<script type="text/javascript" src="<spring:url value="/static/app/ui-forms-modals.js"/>"></script>
	<script type="text/javascript" src="<spring:url value="/static/app/main-controller.js"/>" ></script>
	

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>

<body ng-app="myApp" ng-controller="mainController as main">

<div class="row" >
	<div class="col-xs-12">
		<h2 class="page-header">Pagination - basic example</h2>
      
		<div class="row">
			<div class="col-md-6">
				<div class="bs-callout bs-callout-info">
				<h4>Overview</h4>
				<p><code>ngTable</code> supplies a pager to browse one "chunk" of data at a time. </p>
				<p>Supply an initial paging configuration to the <code>NgTableParams</code> constructor call. As required, you can then change this configuration "on the fly".</p>
				</div>
			</div>
	        <div ng-controller="NetworkActionsController" class="col-md-6">
	          	<button type="button" class="btn btn-default btn-sm" ng-click="openEditNetwork(true, null, networksTableRefreshCallback)">Crear una nueva red</button>
		    
			    <div class="btn-group" uib-dropdown>
			      <button id="split-button" type="button" class="btn btn-danger">Action</button>
			      <button type="button" class="btn btn-danger" uib-dropdown-toggle>
			        <span class="caret"></span>
			        <span class="sr-only">Split button!</span>
			      </button>
			      <ul uib-dropdown-menu role="menu" aria-labelledby="split-button">
			        <li role="menuitem"><a href="#">Action</a></li>
			        <li role="menuitem"><a href="#">Another action</a></li>
			        <li role="menuitem"><a href="#">Something else here</a></li>
			        <li class="divider"></li>
			        <li role="menuitem"><a href="#">Separated link</a></li>
			      </ul>
			    </div>
	        </div>
		</div> <!-- End Row cabecera -->
      
		<div class="row">
	      	<div class="col-xs-12">
				<table ng-table="networksTable" class="table table-bordered table-striped table-condensed">
					<tr ng-repeat="network in $data track by network.acronym">
						<td width="30" style="text-align: left" header="'ng-table/headers/checkbox.html'">
					    	<input type="checkbox" ng-model="networks.selected[network.acronym]" />
					    </td>
					  	<td data-title="'Acrónimo'"    filter="{acronym: 'text'}" sortable="'acronym'">{{network.acronym}}</td>
					    <td data-title="'Repositorio'" filter="{name: 'text'}" sortable="'name'">{{network.name}}</td>
					    <td data-title="'Institución'" filter="{institution: 'text'}" sortable="'institutionName'">{{network.institution}}</td>
					    <td data-title="''">
					    	<ul>
					    		<li>{{network.lstSnapshotStatus}}</li>
					    		<li>{{network.lstSize}}</li>
					    		<li>{{network.lstTransformedSize}}</li>
					    		<li>{{network.lstValidSize}}</li>
					    		<li>{{network.lstSnapshotDate}}</li>
					    	</ul>
					    </td>
					    
					    <td ng-controller="NetworkActionsController">
					     	  <button class="btn btn-primary btn-sm" ng-click="addItem('test')">   <span class="glyphicon glyphicon-ok"></span>		</button>
					          <button class="btn btn-primary btn-sm" ng-click="main.cancel(network, networkForm)"> <span class="glyphicon glyphicon-remove"></span> </button>
					          <button class="btn btn-primary btn-sm" ng-click="openEditNetwork(false,network.networkID,networksTableRefreshCallback)"> 	   <span class="glyphicon glyphicon-pencil"></span>	</button>
					          <button class="btn btn-danger  btn-sm" ng-click="main.del(network)">			   <span class="glyphicon glyphicon-trash"></span>	</button> 
					    </td>
				  </tr>
				</table>
	      	</div>    
  		</div>
	</div>
</div>

<!----------------- TEMPLATES ------------------------------ -->

<script type="text/ng-template" id="network-edit-tpl.html">
<div class="modal-header">
	<h3 class="modal-title">Editando: {{network.name}}</h3>
</div>

<div class="modal-body">

	<!-- TABSET -->
 	<uib-tabset active="activeJustified" justified="true">0
    	<uib-tab index="0" heading="Principal">
			<form name="networkEditForm" sf-schema="network_schema" sf-form="network_form" sf-model="network_model" ng-submit="onSubmit(networkEditForm)" ></form>
			<div ng-if="saved" class="alert alert-success" role="alert">Los datos han sido grabados con éxito</div>
			<div ng-if="!is_form_valid" class="alert alert-danger" role="alert">Los datos no son válidos, no se han grabado</div>
			<div ng-if="save_error" class="alert alert-danger" role="alert">No se han podido guardar los datos - {{save_error_message}}</div>
		</uib-tab>
    	<uib-tab index="1" heading="Propiedades">
			<form name="networkPropertiesEditForm" sf-schema="np_schema" sf-form="np_form" sf-model="np_model" ng-submit="onSubmit(networkEditForm)" ></form>
		</uib-tab>
    	<uib-tab index="2" heading="Orígenes">
			<form name="originsEditForm" sf-schema="origins_schema" sf-form="origins_form" sf-model="origins_model" ng-submit="onSubmit(networkEditForm)" ></form>
			<p>{{origins_model}}</p>
		</uib-tab>
	</uib-tabset>

</div>

<div class="modal-footer">
	<button class="btn btn-success" type="button" ng-click="onSubmit(networkEditForm)">Grabar</button>
	<button class="btn btn-warning" type="button" ng-click="cancel()">Cerrar</button>
</div>
</script>

<script type="text/ng-template" id="ng-table/headers/checkbox.html">
<input type="checkbox" ng-model="networks.areAllSelected" id="select_all" name="filter-checkbox" value="" />
</script>

<!----------------- FIN TEMPLATES -------------------------------->

	
</body>

</html>
