<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "http://java.sun.com/products/javahelp/helpset_2_0.dtd">

<helpset version="1.0">

  <!-- title -->
  <title>OABuilder Help</title>

  <!-- maps -->
  <maps>
     <homeID>top</homeID>
     <mapref location="map.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Contents</label>
    <type>javax.help.TOCView</type>
    <data>toc.xml</data>
  </view>

  <view>
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>index.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>
  
<!-- 2007/05/18 commented out, JavaWebstart throws a security exception
                since this will store favorites in an external xml file.
                see: http://archives.java.sun.com/cgi-bin/wa?A2=ind0402&L=javahelp-interest&P=3416
                
  <view>
    <name>Favorites</name>
    <label>Favorites</label>
    <type>javax.help.FavoritesView</type>
  </view>
-->

  <presentation default="true" displayviewimages="false">
     <name>main window</name>
     <size width="700" height="575" />
     <location x="20" y="20" />
     <title>OABuilder - Online Help</title>
     <image>AppIcon</image>
     <toolbar>
		<helpaction>javax.help.BackAction</helpaction>
		<helpaction>javax.help.ForwardAction</helpaction>
		<helpaction>javax.help.SeparatorAction</helpaction>
		<helpaction>javax.help.HomeAction</helpaction>
		<helpaction>javax.help.ReloadAction</helpaction>
		<helpaction>javax.help.SeparatorAction</helpaction>
		<helpaction>javax.help.PrintAction</helpaction>
		<helpaction>javax.help.PrintSetupAction</helpaction>
     </toolbar>
  </presentation>
  <presentation>
     <name>main</name>
     <size width="400" height="400" />
     <location x="200" y="200" />
     <title>OABuilder Online Help</title>
  </presentation>

</helpset>
