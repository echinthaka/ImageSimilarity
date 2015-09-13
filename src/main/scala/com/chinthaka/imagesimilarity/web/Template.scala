package com.chinthaka.imagesimilarity.web

import scala.xml.Node

object Template {

  def page(title:String, content:Seq[Node], url: String => String = identity _, head: Seq[Node] = Nil, scripts: Seq[String] = Seq.empty, defaultScripts: Seq[String] = Seq("/assets/js/jquery.min.js", "/assets/js/bootstrap.min.js")) = {
    <html lang="en">
      <head>
        <title>{ title }</title>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <meta name="description" content="" />
        <meta name="author" content="" />

        <!-- Le styles -->
        <link href="/assets/css/bootstrap.css" rel="stylesheet" />
        <link href="/assets/css/bootstrap-responsive.css" rel="stylesheet" />
        <link href="/assets/css/syntax.css" rel="stylesheet" />
        <link href="/assets/css/scalatra.css" rel="stylesheet" />

        <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
        <!--[if lt IE 9]>
            <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
          <![endif]-->
        {head}
      </head>

      <body>
        <div class="navbar navbar-inverse navbar-fixed-top">
          <div class="navbar-inner">
            <div class="container">
              <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </a>
              <div class="nav-collapse collapse">

              </div><!--/.nav-collapse -->
            </div>
          </div>
        </div>

        <div class="container">
          <div class="content">
            <div class="page-header">
              <h1>{ title }</h1>
            </div>
            {content}
            <hr/>
          </div> <!-- /content -->
        </div> <!-- /container -->

        <!-- Le javascript
            ================================================== -->
        <!-- Placed at the end of the document so the pages load faster -->
        { (defaultScripts ++ scripts) map { pth =>
        <script type="text/javascript" src={pth}></script>
      } }

      </body>

    </html>
  }
}
