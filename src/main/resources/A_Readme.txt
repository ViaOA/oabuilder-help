

jar:file:/c:ajar.jar!/MyMap.jar

1) help.hs - defines help information
File is pretty much static, only need to change the 3 <title> tags

2) map.jhm - maps names (targets) to urls

3) toc.xml - table of contents, mapped to targets

4) index.xml - maps words to targets

5) jindexer to create index files
C:\projects\java\jh2.0\javahelp\bin\jhindexer - used to generate search index
Specify the top-level folders as arguments to the jhindexer command, as follows:
jhindexer dir1 dir2 dir3

5) Create html pages to use

6) deploy with jh.jar file

7) Viewer - to view/run a help file.  Double click using Windows Explorer.
it will run c:\projects\java\hsviewer.bat.
hsviewer.bat
java -jar C:\projects\java\jh2.0\demos\bin\hsviewer.jar -helpset %1

