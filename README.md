# constant-maven-plugin
Export properties to constants sources

## Example

```xml
<plugin>
   <groupId>com.github.spirylics</groupId>
   <artifactId>constant-maven-plugin</artifactId>
   <version>1.0</version>
   <executions>
       <execution>
           <id>generate-constants</id>
           <goals>
               <goal>java</goal>
           </goals>
           <configuration>
               <directory>${project.build.directory}/gen</directory>
               <name>com.github.spirylics.R</name>
               <includes>
                    <include>constant.*</include>
               </includes>
           </configuration>
       </execution>
   </executions>
</plugin>
```

Should generated a java class in 'directory' with constants from maven properties matching with includes.