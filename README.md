# Alfresco Content Stripper

This is a tool used to get a list with content paths in alf_data for content needed to preserve consistency in the repository.
This is very usable if there for example is a need to debug an alfresco installation where the error is not easy to replicate
with the small dataset of a development environment. This way you can transfer a whole production contentstore (with the major content property stripped). The structure will be intact but all files will be of 0 bytes (except the ones needed as explained above).

The content paths listed are 
 * All content in Company Home/Data Dictionary
 * All preference values of all users
 * If a site is provided: all content in the site
 
 The tool is exposed as a webscript on /alfresco/service/admin/excludelist you could also add the optional parameter site to
include content from a particular site in the path list. Example: /alfresco/service/admin/excludelist?shortName=testsite will return 
something like:

```text
2016/10/18/15/16/fb4a374d-1fe3-4a4e-a16d-df2544f5864c.bin
2016/10/18/15/16/536f0e41-17e6-479e-805a-2bf7c95363d9.bin
2016/10/18/15/16/b0537345-1ace-473c-9c14-9b42f03e2e91.bin
2016/10/18/15/16/68563683-d41c-432a-9bf2-be0e31381a40.bin
2016/10/18/15/16/94826d2e-387b-43ae-9b1d-5edc34549757.bin
2016/10/18/15/16/9e6749fb-d41a-4586-8e83-467e03517e53.bin
2016/10/18/15/16/59485495-c409-43ed-9752-be89b975930b.bin
2016/10/18/15/16/d80fcd79-8235-46ae-aaf3-65c814f288ef.bin
```

Building & Installation
------------
The build produces one jar file. Attach it to your own maven project using dependencies or put it under tomcat/shared/lib.

SDK 1 and SDK 2

Repository dependency:
```xml
<dependency>
  <groupId>org.redpill-linpro.alfresco.contentstripper</groupId>
  <artifactId>alfresco-contentstripper</artifactId>
  <version>1.0.0</version>
</dependency>
```


SDK 3

Platform/Repository module (parent pom):
```xml
<moduleDependency>
  <groupId>org.redpill-linpro.alfresco.contentstripper</groupId>
  <artifactId>alfresco-contentstripper</artifactId>
  <version>1.0.0</version>
</moduleDependency>
```

Maven repository:
```xml
<repository>
  <id>redpill-public</id>
  <url>http://maven.redpill-linpro.com/nexus/content/groups/public</url>
</repository>
```

The jar files are also downloadable from: https://maven.redpill-linpro.com/nexus/index.html#nexus-search;quick~alfresco-systemmessages


License
-------

This application is licensed under the LGPLv3 License. See the [LICENSE file](LICENSE) for details.

Authors
-------

Erik Billerby - Redpill Linpro AB


# Instructions for deployment

 * Add the dependency instructed above
 * Make sure python is installed in the environment that contains the content store path.
 * Put the mkdtrunc.py script in the environment where it has access to the content store. (make it executable)
 
  
 
