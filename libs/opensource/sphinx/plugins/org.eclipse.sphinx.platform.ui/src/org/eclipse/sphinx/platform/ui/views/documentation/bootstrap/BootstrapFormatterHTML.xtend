/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.ui.views.documentation.bootstrap

import org.eclipse.sphinx.platform.ui.internal.Activator
import org.eclipse.sphinx.platform.ui.views.documentation.IDocumentationSection

class BootstrapFormatterHTML {

	def static String format(String title, String content, Iterable<IDocumentationSection> sections) {
		pre(title, sections) + content + post()
	}

	def static String pre(String title, Iterable<IDocumentationSection> sections) '''
			<html lang="en">
		 <head>
		   <meta charset="utf-8">
		   <meta http-equiv="X-UA-Compatible" content="IE=edge">
		   <meta name="viewport" content="width=device-width, initial-scale=1.0">
		   <meta name="description" content="">
		   <meta name="author" content="">
		 
		   <title>Sticky Footer Navbar Template for Bootstrap</title>
		
		    <!-- Bootstrap core CSS -->
		    <link href="«"" + Activator.bootstrapDir»/css/bootstrap.css" rel="stylesheet">
		
		    <!-- Custom styles for this template -->
		    <link href="sticky-footer-navbar.css" rel="stylesheet">
		
		   
		   </head>
		
		  <body>
		
		    <!-- Wrap all page content here -->
		    <div id="wrap">
		
		      <!-- Fixed navbar -->
		      <div class="navbar navbar-default navbar-fixed-top" role="navigation">
		        <div class="container">
		          <div class="navbar-header">
		            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
		              <span class="sr-only">Toggle navigation</span>
		              <span class="icon-bar"></span>
		              <span class="icon-bar"></span>
		              <span class="icon-bar"></span>
		            </button>
		            <a class="navbar-brand" href="#">«title» </a>
		          </div>
		          <div class="collapse navbar-collapse">
		            <ul class="nav navbar-nav">
		              
		              «FOR s : sections.filter(x|x.sectionTitle != null)»
		              	<li><a href="#«s.sectionTitle»">«s.sectionTitle»</a></li>
		              «ENDFOR»
		            </ul>
		          </div><!--/.nav-collapse -->
		        </div>
		      </div>
		
		      <!-- Begin page content -->
		      <div class="container">
		        <div class="page-header">
		          <h1></h1>
		        </div>
		        <p class="lead">
		       «sections.filter(x|x.sectionTitle == null || x.sectionTitle.length == 0).head?.sectionBody»</p>
		      </div>
		    </div>
		
			<div class="container">
			    
			
	'''

	def static String post() '''
			</div>
			<div id="footer">
		     <div class="container">
		       <p class="text-muted credit">End of Information.</p>
		     </div>
		    </div>
		
		    <script src="«"" + Activator.getJQueryDir»/jquery-1.11.1.js"></script>
		    <script src="«"" + Activator.bootstrapDir»/js/bootstrap.min.js"></script>
		  </body>
		</html>
	'''
}
